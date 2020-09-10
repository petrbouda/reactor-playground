

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WorkInProgress {

    private final Subscriber subscriber;
    private final int total;

    private volatile int requested;
    private static final AtomicIntegerFieldUpdater<WorkInProgress> REQUESTED =
            AtomicIntegerFieldUpdater.newUpdater(WorkInProgress.class, "requested");

    private int index;

    public WorkInProgress(Subscriber subscriber, int total) {
        this.subscriber = subscriber;
        this.total = total;
    }

    public void increment(int n) {
        int initialRequested;
        int tasks;
        do {
            subscriber.onTrace();
            // Gets initial requested
            initialRequested = requested;
            // Add its own number of requested elements
            tasks = initialRequested + n;
            // Tests if some other thread does a race condition during a processing of DO statement.
        } while (!REQUESTED.compareAndSet(this, initialRequested, tasks));

        // Thread B Successfully inserted its job to another thread
        if (initialRequested > 0) {
            return;
        }

        // This section is processed only by one thread at the certain time.
        process(tasks);
    }

    private void process(int n) {
        int sent = 0;
        int i = index;
        while (true) {
            for (; sent < n && i < total; sent++, i++) {
                subscriber.onNext(i);
            }

            if (i == total) {
                subscriber.onCompleted();
                return;
            }

            // Check if some thread added a job for the current one
            n = requested;
            if (n == sent) {
                // Don't have to be volatile because of memory link between
                // REQUESTED.compareAndSet
                // REQUESTED.addAndGet
                index = i;
                if (REQUESTED.addAndGet(this, -sent) == 0) {
                    return;
                }
                sent = 0;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int n = args.length > 0
                ? Integer.parseInt(args[0])
                : 5000;

        ExecutorService workers = Executors.newFixedThreadPool(5);

        List<String> objects = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        LongAdder traceCounter = new LongAdder();

        Subscriber subscriber = new Subscriber() {
            @Override
            public void onTrace() {
                // Uncomment to see an overall number of iteration to during pushing tasks.
                // We can see that if a tasks processing is a bit longer:
                // - uncomment the print statement inside ON_NEXT
                // then even the contention is significantly lower.
                traceCounter.increment();
            }

            @Override
            public void onNext(int value) {
                // Uncomment print statement!
                // A thread that does a really work has a blocking operation "syscall"
                // that means that other threads has a plenty of time to add their tasks
                // to a processing thread. It results in a one thread doing all the jobs.
                //
                // Result:
                // pool-1-thread-1 - 2
                // pool-1-thread-3 - 3
                // pool-1-thread-5 - 4995
                //
                // If a print statement is commented then the tasks are more evenly distributed.
                //
                // pool-1-thread-1 - 1921
                // pool-1-thread-3 - 562
                // pool-1-thread-2 - 1109
                // pool-1-thread-5 - 862
                // pool-1-thread-4 - 546
                //
                // System.out.println("ON_NEXT " + Thread.currentThread().getName());
                objects.add(Thread.currentThread().getName());
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        WorkInProgress wip = new WorkInProgress(subscriber, n);
        for (int i = 0; i < n; i++) {
            workers.execute(() -> wip.increment(1));
        }

        wait(workers, latch);
        printResult(objects, traceCounter);
    }

    private static void wait(ExecutorService workers, CountDownLatch latch) throws InterruptedException {
        latch.await(5, TimeUnit.SECONDS);
        workers.shutdown();
        workers.awaitTermination(5, TimeUnit.SECONDS);
    }

    private static void printResult(List<String> objects, LongAdder traceCounter) {
        AtomicInteger count = new AtomicInteger();
        objects.stream()
                .collect(Collectors.groupingBy(Function.identity()))
                .forEach((key, value) -> {
                    System.out.println(key + " - " + value.size());
                    count.addAndGet(value.size());
                });

        System.out.println("TOTAL - " + count.get());
        System.out.println("TRACE COUNT - " + traceCounter.longValue());
    }

    private interface Subscriber {
        void onTrace();

        void onNext(int value);

        void onCompleted();
    }
}
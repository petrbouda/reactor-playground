package pbouda.reactor.common;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Merging {

    public static void main(String[] args) {
        DirectProcessor<Integer> processor1 = DirectProcessor.create();
        FluxSink<Integer> sink1 = processor1.sink();

        DirectProcessor<Integer> processor2 = DirectProcessor.create();
        FluxSink<Integer> sink2 = processor2.sink();

        Flux<String> flow1 = Flux.from(processor1)
                .map(val -> Thread.currentThread() + " - flow 1 " + val);

        Flux<String> flow2 = Flux.from(processor2)
                .doOnNext(val -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .map(val -> Thread.currentThread() + " - flow 2 " + val);

        flow1.mergeWith(flow2)
                .map(val -> Thread.currentThread() + " - merger " + val)
                .subscribe(System.out::println);

        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        scheduler.scheduleAtFixedRate(
                () -> sink1.next(counter1.getAndIncrement()), 0, 1, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(
                () -> sink2.next(counter2.getAndIncrement()), 0, 1, TimeUnit.SECONDS);
    }
}

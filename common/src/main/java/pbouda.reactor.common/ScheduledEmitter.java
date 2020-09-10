package pbouda.reactor.common;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

public class ScheduledEmitter {

    public static void main(String[] args) throws InterruptedException {
        Flux.interval(Duration.ofSeconds(1), Schedulers.newSingle("emitter"))
                .log()
                .publishOn(Schedulers.newSingle("processor"))
                // Could not emit tick 2 due to lack of requests
                // (interval doesn't support small downstream requests that replenish slower than the ticks)
                .onBackpressureBuffer(1)
//                .map(v -> {
//
//                })
//                .log()
//                .limitRate(1)
//                .map(i -> i)
//                .delaySequence(Duration.ofSeconds(2))
//                .delayElements(Duration.ofSeconds(2))
//                .onBackpressureBuffer(1)
//                .delayElements(Duration.ofSeconds(5))
//                .buffer()
//                .log()
//                .onBackpressureError()
//                .onBackpressureBuffer(2)
//                .log()
//                .limitRate(1)
                .log()
                .subscribe(new CustomSubscriber());

        Thread.currentThread().join();
    }

    private static class CustomSubscriber extends BaseSubscriber<Long> {

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            subscription.request(1);
        }

        @Override
        protected void hookOnNext(Long value) {
            System.out.println(value);
//            if (value % 5 == 0) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            upstream().request(1);
//            } else {
//                upstream().request(1);
//            }
        }
    }
}

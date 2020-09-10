package pbouda.reactor.common;

import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

public class ApplicationRetry {

    public static void main(String[] args) throws InterruptedException {
        Flux.just(1, 2, 3, 4, 5)
                .map(index -> "Order " + index)
                .filter(order -> {
                    if ("Order 3".equals(order)) {
                        throw new RuntimeException("Order 3 is forbidden");
                    }
                    return true;
                })
                
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5)))
                .subscribe(System.out::println, Throwable::printStackTrace);

        Thread.currentThread().join();
    }
}

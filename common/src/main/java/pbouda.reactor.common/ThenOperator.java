package pbouda.reactor.common;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ThenOperator {

    public static void main(String[] args) {
        first();
        second();
    }

    private static void first() {
        Flux.just("1", "2", "3")
                .map(number -> "transformed-" + number)
                .then(Mono.just("After all"))
                // After transforming all items,
                // we receive "After all" string
                // It swallow completion signal and
                // send a new value below.
                .doOnSuccess(System.out::println)
                // "After all" is a regular emitted
                // event.
                .subscribe(System.out::println);
    }

    private static void second() {
        Flux.just("1", "2", "3")
                .map(number -> "transformed-" + number)
                .then()
                // result is null => void
                // It consumes all items and replays
                // completion signal
                .doOnSuccess(System.out::println)
                // it's never called
                .subscribe(System.out::println);
    }

}

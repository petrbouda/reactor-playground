package pbouda.reactor.common;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class SubscriberContext {

    public static void main(String[] args) {
        Flux.just("1", "2", "3")
                .buffer()
                .doOnEach(signal -> {
                    if (signal.isOnNext()) {
                        System.out.println(signal.getContext());
                    }
                })
                .subscriberContext(Context.of("app", "testing"))
                .subscribe(System.out::println);

    }
}

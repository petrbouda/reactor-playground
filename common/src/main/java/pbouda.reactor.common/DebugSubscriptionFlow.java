package pbouda.reactor.common;

import reactor.core.publisher.Mono;

public class DebugSubscriptionFlow {

    public static void main(String[] args) {
        String key = "message";
        Mono.just("Hello")
                .doOnSubscribe(s1 -> System.out.println())
                .flatMap(s1 -> Mono.subscriberContext()
                        .doOnSubscribe(s2 -> System.out.println())
                        .map(ctx -> {
                            return s1 + " " + ctx.get(key);
                        })
                        .doOnSubscribe(s2 -> System.out.println())
                )
                .doOnSubscribe(s1 -> System.out.println())
                .flatMap(s1 -> Mono.subscriberContext()
                        .doOnSubscribe(s2 -> System.out.println())
                        .map(ctx -> {
                            return s1 + " " + ctx.get(key);
                        })
                        .doOnSubscribe(s2 -> System.out.println())
                        .subscriberContext(ctx -> {
                            return ctx.put(key, "Reactor");
                        })
                        .doOnSubscribe(s2 -> System.out.println())
                )
                .doOnSubscribe(s1 -> System.out.println())
                .subscriberContext(ctx -> {
                    return ctx.put(key, "World");
                })
                .doOnSubscribe(s1 -> System.out.println())
                .subscribe(
                        entity -> {
                            System.out.println(entity);
                        },
                        throwable -> throwable.printStackTrace()
                );
    }
}

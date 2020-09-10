package pbouda.reactor.common;

import reactor.core.publisher.Mono;

public class DebugAndContextExample {

    public static void main(String[] args) {
        /*
         https://projectreactor.io/docs/core/release/reference/#context
         https://projectreactor.io/docs/core/release/reference/#faq.mdc
         https://github.com/reactor/reactor-tools
        */

//        innerSequence();
        outerSequence();
    }

    public static void outerSequence() {
        /*
         * Global checkpoint! Only if an exception occurs.
         * +
         * Adds support for IntelliJ in Debugger Window
         */
//        Hooks.onOperatorDebug();

        String key = "message";
        Mono.just("Hello")
                /*
                 * Logs all communication between those two operators
                 * 2020-07-21 21:02:12,455 [main] INFO  r.M.J.1 - | onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
                 * 2020-07-21 21:02:12,457 [main] INFO  r.M.J.1 - | request(unbounded)
                 * 2020-07-21 21:02:12,458 [main] INFO  r.M.J.1 - | onNext(Hello)
                 * first
                 * second
                 * Hello Reactor World
                 * 2020-07-21 21:02:12,466 [main] INFO  r.M.J.1 - | onComplete()
                 */
                .log()
                .flatMap(s -> {
                    System.out.println("first");
                    return Mono.subscriberContext()
                            .map(ctx -> {
                                return s + " " + ctx.get(key);
                            });
                })
                .subscriberContext(ctx -> {
                    return ctx.put(key, "Reactor");
                })

                /*
                 * The operator below sees `World`
                 * => The reason is that the Context is associated to
                 *    the Subscriber and each operator accesses the
                 *    Context by requesting it from its downstream
                 *    Subscriber.
                 */
                .flatMap(s -> {
                    System.out.println("second");
                    return Mono.subscriberContext()
                            .map(ctx -> {
                                // throw new RuntimeException(" --- ");
                                return s + " " + ctx.get(key);
                            });
                })
                .subscriberContext(ctx -> {
                    return ctx.put(key, "World");
                })
                /*
                 * The same output as Hooks.onOperatorDebug();
                 * it's just locally activated instead of globally,
                 * always place this operator at the end to of pipeline
                 * to activate `reactor.core.publisher.Hooks.GLOBAL_TRACE`
                 * (Stacktrace recording)
                 */
                // .checkpoint()
                .subscribe(
                        entity -> {
                            System.out.println(entity);
                        },
                        throwable -> throwable.printStackTrace()
                );
    }

    private static void innerSequence() {
        String key = "message";
        Mono.just("Hello")
                .flatMap(s ->
                        // `subscribe` to the object below is happened when
                        // this lambda is being processed and returned Mono
                        // object.
                        Mono.subscriberContext()
                                .map(ctx -> {
                                    return s + " " + ctx.get(key);
                                })
                )
                .flatMap(s ->
                        Mono.subscriberContext()
                                .map(ctx -> {
                                    return s + " " + ctx.get(key);
                                })
                                .subscriberContext(ctx -> {
                                    return ctx.put(key, "Reactor");
                                })
                )
                .subscriberContext(ctx -> {
                    return ctx.put(key, "World");
                })
                .subscribe(
                        entity -> {
                            System.out.println(entity);
                        },
                        throwable -> throwable.printStackTrace()
                );
    }
}

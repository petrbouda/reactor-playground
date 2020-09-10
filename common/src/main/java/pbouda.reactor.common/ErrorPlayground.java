package pbouda.reactor.common;

import reactor.core.publisher.Flux;

public class ErrorPlayground {

    public void testDefer() {
    }

    /*
     In the preceding code, we are now throwing IllegalStateException, instead of RuntimeException.
     IllegalStateException is a sub-type of RuntimeException. The subscriber is configured for both
     of these exceptions. It is important to note the order of the configuration here.
     RuntimeException has been configured first, with a default value of 0, and the IllegalStateException
     with the value -1. Reactor will match the thrown exception against RuntimeException
     */
    public void testErrorReturn() {
        Flux.just()
                .onErrorReturn(RuntimeException.class, 0L)
                .onErrorReturn(IllegalStateException.class, -1L)
                .subscribe(System.out::println);
    }

    /*
     Similar to the OnErrorReturn operator, there is the OnErrorResume operator, which provides a fallback
     value stream instead of a single fallback value. In the event of an error, the fallback stream is returned.
     The original error event is not propagated to the error callback.
     */
    public void testErrorResume() {
        Flux.just()
                .onErrorResume(x -> Flux.just(0L, -1L, -2L))
                .subscribe(System.out::println);
    }

    public void testErrorMap() {
        Flux.just()
                .onErrorMap(x -> new IllegalStateException("Publisher threw error", x))
                .subscribe(System.out::println, System.out::println);
    }

}

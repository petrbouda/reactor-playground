package pbouda.reactor.common;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.io.Closeable;

public class ClosingPlayground {

    /*
     Similar to the doOnError life cycle hook, there is the doOnTerminate hook.
     This is a generic hook that is invoked for on completion and on error
     stream termination events. Unlike the specific error hook, which provides
     the exception thrown, this hook does not provide any kind of input.
     It just executes the lambda provided. It is important to note that the
     doOnTerminate hook is invoked as soon as we receive termination events.
     It does not wait for the error callback to be processed.
     */
    public void testDoTerminate() {
        // Removed for brevity
        Flux.empty()
                .doOnTerminate(() -> System.out.println("Terminated"))
                .subscribe(System.out::println, Throwable::printStackTrace);
    }

    /*
      Similar to the doOnError life cycle hook, there is the doFinally hook.
      This hook  is invoked post-stream completion. The hook executes the
      lambda provided. It is important to note that the doFinally hook is
      invoked post-stream close callback processing, unlike the previously
      discussed doOnTerminate hook, which is invoked as soon as we received
      the close events.
     */
    public void testDoFinally() {
        Flux<Long> fibonacciGenerator = Flux.generate(() ->
                Tuples.of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0)
                sink.error(new RuntimeException("Value out of bounds"));
            else
                sink.next(state.getT1());

            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        fibonacciGenerator
                .doFinally(x -> System.out.println("invoking finally"))
                .subscribe(System.out::println, Throwable::printStackTrace);
    }

    /*
      As an alternative to the doFinally hook, there is the Flux.using API.
      This API configures a resource mapped for a publisher. It also configures
      a callback lambda, which is invoked with the respective publisher resource
      upon stream closure. This is synonymous with the try-with-resource Java API
     */
    public void testUsingMethod() {
        Flux<Long> fibonacciGenerator = Flux.generate(() ->
                Tuples.of(0L, 1L), (state, sink) -> {
            if (state.getT1() < 0)
                sink.complete();
            else
                sink.next(state.getT1());

            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });

        Closeable closable = () -> System.out.println("closing the stream");
        Flux.using(() -> closable, x -> fibonacciGenerator, e -> {
            try {
                e.close();
            } catch (Exception e1) {
                throw Exceptions.propagate(e1);
            }
        }).subscribe(System.out::println);
    }
}

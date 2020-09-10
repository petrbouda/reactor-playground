package pbouda.reactor.common;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.io.IOException;

public class AdditionsPlayground {

    public void testCheckedExceptions() {
        Flux<Long> fibonacciGenerator = Flux.generate(() ->
                Tuples.of(0L, 1L), (state, sink) -> {
            try {
                raiseCheckedException();
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
            return Tuples.of(state.getT2(), state.getT1() + state.getT2());
        });
        fibonacciGenerator
                .subscribe(System.out::println, Exceptions::unwrap);
    }

    void raiseCheckedException() throws IOException {
        throw new IOException("Raising checked Exception");
    }

    private static void testWindowsFixedSize() {
        Flux<Long> fibonacciGenerator = Flux.generate(
                () -> Tuples.of(0L, 1L), (state, sink) -> {
                    if (state.getT1() < 0)
                        sink.complete();
                    else
                        sink.next(state.getT1());
                    return Tuples.of(state.getT2(), state.getT1() + state.getT2());
                });
        fibonacciGenerator
                .window(10)
                // Flux of elements
                // More efficient then buffers
                .concatMap(x -> x)
                .subscribe(x -> System.out.print(x + " "));
    }

    /*
      Let's run the preceding test case and validate the output printed on the console.
      Both error functions are invoked simultaneously, as follows:
     */
    public void testDoError() {
        // throw an exception
        Flux.just()
                .doOnError(System.out::println)
                .subscribe(System.out::println, Throwable::printStackTrace);
    }
}

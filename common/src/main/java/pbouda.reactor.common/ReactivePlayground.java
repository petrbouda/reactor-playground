package pbouda.reactor.common;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReactivePlayground {

    private static final BaseSubscriber<Long> BASE_SUBSCRIBER = new BaseSubscriber<>() {
        @Override
        protected void hookOnSubscribe(Subscription subscription) {
        }

        @Override
        protected void hookOnNext(Long value) {
        }

        @Override
        protected void hookOnComplete() {
        }

        @Override
        protected void hookOnError(Throwable throwable) {
        }

        @Override
        protected void hookOnCancel() {
        }

    };

    public static void main(String[] args) {
//        synchronousSink();

        taking();
//        Flux.fromIterable(List.of("Petr", "Bouda"))
//                .map(name -> {
//                    System.out.println(name);
//                    return name + " - printed";
//                })
//                .map(printedName -> {
//                    System.out.println(printedName);
//                    return printedName + " final";
//                })
//                .subscribe(f -> {
//                    System.out.println(f);
//                    System.out.println("IT' OVER");
//                });
    }

    private static void synchronousSink() {
        Flux<Long> fibonacciGenerator = Flux.generate(
                () -> Tuples.of(0L, 1L),
                (state, sink) -> {
                    sink.next(state.getT1());
                    System.out.println("generated " + state.getT1());
                    return Tuples.of(state.getT2(), state.getT1() + state.getT2());
                });

        fibonacciGenerator.take(5).subscribe(t -> {
            System.out.println("consuming " + t);
        });
    }

    public void testFibonacciFluxSink() {
        Flux<Long> fibonacciGenerator = Flux.create(e -> {
            long current = 1, prev = 0;
            AtomicBoolean stop = new AtomicBoolean(false);
            e.onDispose(() -> {
                stop.set(true);
                System.out.println("******* Stop Received ****** ");
            });
            while (current > 0) {
                e.next(current);
                System.out.println("generated " + current);
                long next = current + prev;
                prev = current;
                current = next;
            }
            e.complete();
        });
        List<Long> fibonacciSeries = new LinkedList<>();
        fibonacciGenerator.take(50).subscribe(t -> {
            System.out.println("consuming " + t);
            fibonacciSeries.add(t);
        });
        System.out.println(fibonacciSeries);
    }

    private static void filtering() {
        Flux.fromIterable(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                .filterWhen(value -> Mono.just(value).map(v -> v % 2 == 0), 1)
                .subscribe(System.out::println);
    }

    private static void taking() {
        Flux.fromIterable(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                .takeLast(1)
                .subscribe(System.out::println);
    }

    private static void rest() {
        Flux<Long> fibonacciGenerator = Flux.generate(
                () -> Tuples.of(0L, 1L),
                (state, sink) -> {
                    sink.next(state.getT1());
                    return Tuples.of(state.getT2(), state.getT1() + state.getT2());
                });

        fibonacciGenerator
                .take(10)
                .collectList()
                .subscribe(System.out::println);

        fibonacciGenerator
                .take(10)
                .reduce(Long::sum)
                .subscribe(System.out::println);

        fibonacciGenerator
                .take(10)
                .concatWith(Flux.just(-1L,-2L,-3L,-4L))
                .subscribe(System.out::println);
    }
}

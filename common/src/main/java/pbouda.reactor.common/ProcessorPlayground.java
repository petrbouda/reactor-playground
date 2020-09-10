package pbouda.reactor.common;


import reactor.core.publisher.*;
import reactor.extra.processor.TopicProcessor;
import reactor.extra.processor.WorkQueueProcessor;
import reactor.util.concurrent.Queues;
import reactor.util.function.Tuples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProcessorPlayground {

    /*
      DirectProcessor is the simplest of the processors. This processor connects a processor
      to a subscriber, and then directly invokes the Subscriber.onNext method. The processor
      does not offer any backpressure handling.
     */
    private static void direct() {
        DirectProcessor<Long> data = DirectProcessor.create();
        data.take(2).subscribe(System.out::println);
        data.onNext(10L);
        data.onNext(11L);
        data.onNext(12L);
    }

    /*
      The UnicastProcessor type is like the DirectProcessor, in terms of invocations of the
      Subscriber.onNext method. It invokes the subscriber method directly. However, unlike
      the DirectProcessor, the UnicastProcessor is capable of backpressure. Internally,
      it creates a queue to hold undelivered events. We can also provide an optional external
      queue to buffer the events. After the buffer is full, the processor starts to reject
      elements. The processor also makes it possible to perform cleanup for every rejected element.

      ---

      While UnicastProcessor provides backpressure capability, a major limitation is
      that only a single subscriber can be worked with.
     */
    private static void unicast() {
        UnicastProcessor<Long> data = UnicastProcessor.create();
        data.subscribe(System.out::println);
        data.sink().next(10L);
    }

    /*
      EmitterProcessor is a processor that can be used with several subscribers.
      Multiple subscribers can ask for the next value event, based on their individual
      rate of consumption. The processor provides the necessary backpressure support
      for each subscriber.
     */
    private static void emitter() {
        EmitterProcessor<Long> data = EmitterProcessor.create(1);
        data.subscribe(System.out::println);
        FluxSink<Long> sink = data.sink();
        sink.next(10L);
        sink.next(11L);
        sink.next(12L);
        data.subscribe(System.out::println);
        sink.next(13L);
        sink.next(14L);
        sink.next(15L);
    }

    /*
      ReplayProcessor is a special-purpose processor, capable of caching and replaying
      events to its subscribers. The processor also has the capability of publishing
      events from an external publisher. It consumes an event from the injected
      publisher and synchronously passes it to the subscribers.

      ReplayProcessor can cache events for the following scenarios:
        All events
        A limited count of events
        Events bounded by a specified time period
        Events bounded by a count and a specified time period
        The last event only
     */
    private static void replay() {
        ReplayProcessor<Long> data = ReplayProcessor.create(3);
        data.subscribe(System.out::println);
        FluxSink<Long> sink = data.sink();
        sink.next(10L);
        sink.next(11L);
        sink.next(12L);
        sink.next(13L);
        sink.next(14L);
        data.subscribe(System.out::println);
    }

    /*
      TopicProcessor is a processor capable of working with multiple
      subscribers, using an event loop architecture. The processor delivers
      events from a publisher to the attached subscribers in an asynchronous
      manner, and honors backpressure for each subscriber by using the RingBuffer
      data structure. The processor is also capable of listening to events from
      multiple publishers. This is illustrated in the following diagram:
     */
    private static void topic() {
        TopicProcessor<Long> data = TopicProcessor.<Long>builder()
                .executor(Executors.newFixedThreadPool(2)).build();
        data.subscribe(System.out::println);
        data.subscribe(System.out::println);
        FluxSink<Long> sink = data.sink();
        sink.next(10L);
        sink.next(11L);
        sink.next(12L);
    }

    /*
      The WorkQueueProcessor type is similar to the TopicProcessor, in that it
      can connect to multiple subscribers. However, it does not deliver all
      events to each subscriber. The demand from every subscriber is added to
      a queue, and events from a publisher are sent to any of the subscribers.
      The model is more like having listeners on a JMS queue; each listener
      consumes a message when finished. The processor delivers messages to
      each of the subscribers in a round-robin manner. The processor is also
      capable of listening to events from multiple publishers.
     */
    private static void workQueue() {
        WorkQueueProcessor<Long> data = WorkQueueProcessor.<Long>builder().build();
        data.subscribe(t -> System.out.println("1. " + t));
        data.subscribe(t -> System.out.println("2. " + t));
        FluxSink<Long> sink = data.sink();
        sink.next(10L);
        sink.next(11L);
        sink.next(12L);
    }

    private static void hotPublisher() throws InterruptedException {
        final UnicastProcessor<Long> hotSource = UnicastProcessor.create();
        final Flux<Long> hotFlux = hotSource.publish().autoConnect();
        hotFlux.take(5).subscribe(t -> System.out.println("1. " + t));

        CountDownLatch latch = new CountDownLatch(2);
        new Thread(() -> {
            int c1 = 0, c2 = 1;
            while (c1 < 1000) {
                hotSource.onNext((long) c1);
                int sum = c1 + c2;
                c1 = c2;
                c2 = sum;
                if (c1 == 144) {
                    hotFlux.subscribe(t -> System.out.println("2. " + t));
                }
            }
            hotSource.onComplete();
            latch.countDown();
        }).start();

        latch.await();
    }
}

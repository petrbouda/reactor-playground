package pbouda.reactor.rabbitmq;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.UnicastProcessor;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;

public class RabbitSender implements AutoCloseable {

    private final Sender sender;
    private final String exchange;
    private final FluxSink<OutboundMessage> sink;

    public RabbitSender(SenderOptions senderOptions, String exchange) {
        this.exchange = exchange;
        this.sender = RabbitFlux.createSender(senderOptions);

        // Supports multiple multiple producers (multiple threads emitting the events)
        // Supports only one subscriber / another subscriber throws the exception
        // Contains an internal queue for undelivered message. If the buffer is full,
        // the processor starts to reject elements
        UnicastProcessor<OutboundMessage> processor = UnicastProcessor.create();
        this.sink = processor.sink();

        // Processing is implemented using WIP (Work-in-Progress) pattern, that means
        // that there is a chance that the the current thread will process the sending
        // operation. However, in case of "closed" RabbitMQ, current thread will be
        // blocked on a Retry mechanism (Simple retry implemented using Thread.sleep)
        Flux<OutboundMessage> outboundProcessor = processor
                .publishOn(Schedulers.newSingle("rabbitmq-publisher"));

        // Default ExceptionHandler does not log the exception, just retries invocations
        // every 200ms and 10sec in total.
//        SendOptions sendOptions = new SendOptions()
//                   .exceptionHandler();

        // By default, Sender#send* methods open a new Channel for every call. This is
        // OK for long-running calls, e.g. when the flux of outbound messages is infinite.
        // For workloads whereby Sender#send* is called often for finite, short flux of
        // messages, opening a new Channel every time may not be optimal.
        //
        // The current situation is FLUX of infinite messages thanks to Processor.
        //
        // ChannelPool channelPool = ChannelPoolFactory.createChannelPool(
        //         connectionMono,
        //         new ChannelPoolOptions().maxCacheSize(5)
        // );
        // sender.send(outboundFlux, new SendOptions().channelPool(channelPool));

//        this.sender.send(outboundProcessor, sendOptions)
        this.sender.send(outboundProcessor)
                .subscribe();
    }

    public void send(String message) {
        sink.next(new OutboundMessage(exchange, "", message.getBytes()));
    }

    @Override
    public void close() {
        sender.close();
    }
}

package pbouda.reactor.rabbimq;

import com.rabbitmq.client.Delivery;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;

import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

public class RabbitReceiver implements AutoCloseable {

    private final Receiver receiver;
    private final String queue;

    public RabbitReceiver(ReceiverOptions receiverOptions, String queue) {
        this.receiver = RabbitFlux.createReceiver(receiverOptions)
        ;
        this.queue = queue;
    }

    public Flux<Delivery> start() {
        return receiver.consumeAutoAck(queue);
    }

    public <V> Flux<V> startWithTransformer(Function<Flux<Delivery>, ? extends Publisher<V>> transformer) {
        return receiver.consumeAutoAck(queue)
                .transform(transformer);
    }

    @Override
    public void close() throws Exception {
        receiver.close();
    }
}

package pbouda.reactor.rabbimq;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.rabbitmq.ReceiverOptions;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ApplicationReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReceiver.class);

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");
        connectionFactory.useNio();

        /**
         * TODO: Check threading and provide a thread-factory
         *
         * {@link com.rabbitmq.client.impl.ConsumerWorkService}
         */
        ReceiverOptions receiverOptions = new ReceiverOptions()
                // .connectionClosingTimeout()
                //  .connectionMonoConfigurator(connectionFactory -> connectionFactory.metrics())
                .connectionFactory(connectionFactory)
                .connectionSupplier(cf -> cf.newConnection(List.of(new Address("localhost", 5672))));

        RabbitReceiver receiver = new RabbitReceiver(receiverOptions, "test");

        /*
         * A delayed start can be implemented using `subscribe` method.
         */
        // Function<Flux<Delivery>, Flux<String>> transformer = delivery ->
        //         delivery.map(d -> new String(d.getBody(), StandardCharsets.ISO_8859_1))
        //                 .map(String::toUpperCase);
        //
        // rabbitReceiver.startWithTransformer(transformer)
        //         .subscribe();

        receiver.start()
                .map(d -> new String(d.getBody(), StandardCharsets.ISO_8859_1))
                .map(String::toUpperCase)
                .subscribe(
                        entity -> LOG.info("Received: " + entity),
                        throwable -> LOG.error("Error received! " + throwable.getMessage(), throwable),
                        () -> LOG.info("Processing completed! Thank you all guys!")
                );
    }
}

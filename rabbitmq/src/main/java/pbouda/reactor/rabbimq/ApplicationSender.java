package pbouda.reactor.rabbimq;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.rabbitmq.SenderOptions;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

@SpringBootApplication
public class ApplicationSender {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationSender.class);

    public static final String API_URI = "/api";

    @Bean(destroyMethod = "close")
    public RabbitSender rabbitSender(
            @Value("${rabbitmq.username}") String username,
            @Value("${rabbitmq.password}") String password,
            @Value("${rabbitmq.uri}") String rabbitmqUri,
            @Value("${rabbitmq.exchange}") String exchange) {

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.useNio();

        String[] parts = rabbitmqUri.split(":");
        Address address = new Address(parts[0], Integer.parseInt(parts[1]));
        SenderOptions senderOptions = new SenderOptions()
                .channelCloseHandler((signal, channel) ->
                        LOG.info("RabbitMQ Channel is being closed: signal '{}' channel-number '{}'",
                                signal, channel.getChannelNumber()))
                .connectionFactory(connectionFactory)
                .connectionSupplier(cf -> cf.newConnection(List.of(address)));
//                .resourceManagementScheduler(Schedulers.elastic())
//                .connectionSubscriptionScheduler()

        return new RabbitSender(senderOptions, exchange);
    }

    @Bean
    RouterFunction<ServerResponse> routes(RabbitSender rabbitSender) {
        var handler = new RequestHandler(rabbitSender);
        RequestPredicate jsonTypes = contentType(APPLICATION_JSON).and(accept(APPLICATION_JSON));

        return RouterFunctions.route()
                .POST(API_URI, jsonTypes, handler::post)
                .onError(Throwable.class, new ExceptionHandler())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApplicationSender.class);
        application.addInitializers(new RabbitMqInitializer());
        application.run(args);
    }
}

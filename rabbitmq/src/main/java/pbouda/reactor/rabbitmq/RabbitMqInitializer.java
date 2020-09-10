package pbouda.reactor.rabbitmq;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.testcontainers.containers.RabbitMQContainer;

import java.util.Map;

public class RabbitMqInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final RabbitMQContainer CONTAINER = new RabbitMQContainer()
            .withExchange("test", "fanout")
            .withQueue("test")
            .withBinding("test", "test");

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        CONTAINER.start();
        System.out.printf("RabbitMQ started! %s %s\n",
                CONTAINER.getAmqpPort(),
                CONTAINER.getHttpUrl());

        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> port = Map.of(
                "rabbitmq.uri", "localhost:" + CONTAINER.getAmqpPort(),
                "rabbitmq.username", CONTAINER.getAdminUsername(),
                "rabbitmq.password", CONTAINER.getAdminPassword(),
                "rabbitmq.exchange", "test");
        propertySources.addFirst(new MapPropertySource("rabbit-map", port));
    }
}
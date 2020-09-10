package pbouda.reactor.r2dbc;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.client.SSLMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.testcontainers.containers.CockroachContainer;
import reactor.netty.resources.LoopResources;
import reactor.netty.tcp.TcpResources;

import java.time.Duration;
import java.util.Map;

@SpringBootApplication
public class Application {

    private static class CockroachInitializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        public static final CockroachContainer CONTAINER =
                new CockroachContainer("cockroachdb/cockroach")
                        .withInitScript("init-script.sql");

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            CONTAINER.start();

            System.out.printf("CockroachDB started! DB_PORT: %s, HTTP_PORT %s\n",
                    CONTAINER.getMappedPort(26257),
                    CONTAINER.getMappedPort(8080));

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            MutablePropertySources propertySources = environment.getPropertySources();
            Map<String, Object> database = Map.of(
                    "database.host", CONTAINER.getHost(),
                    "database.port", CONTAINER.getMappedPort(26257),
                    "database.name", "postgres",
                    "database.username", "root",
                    "database.password", "",
                    "database.sslEnabled", false);

            propertySources.addFirst(new MapPropertySource("cockroach-map", database));
        }
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.addInitializers(new CockroachInitializer());
        application.run(args);
    }

    @Bean(destroyMethod = "dispose")
    public ConnectionPool connectionFactory(
            @Value("${database.host}") String host,
            @Value("${database.port:26257}") int port,
            @Value("${database.username:kyc}") String username,
            @Value("${database.password:kyc}") String password,
            @Value("${database.name:kyc}") String name,
            @Value("${database.sslEnabled:true}") boolean sslEnabled,
            @Value("${database.initConnections:5}") int initConnections,
            @Value("${database.maxConnections:10}") int maxConnections,
            @Value("${database.maxIdleInSec:60}") int maxIdleInSec,
            @Value("${database.registerJmx:true}") boolean registerJmx) {

        LoopResources loopResources = null;
        TcpResources.set(loopResources);

        PostgresqlConnectionConfiguration configuration =
                PostgresqlConnectionConfiguration.builder()
                        .applicationName("kyc")
                        .sslMode(sslEnabled ? SSLMode.REQUIRE : SSLMode.DISABLE)
                        .host(host)
                        .port(port)
                        .username(username)
                        .password(password)
                        .database(name)
                        .build();

        ConnectionPoolConfiguration poolConfiguration =
                ConnectionPoolConfiguration.builder(new PostgresqlConnectionFactory(configuration))
                        .maxIdleTime(Duration.ofSeconds(maxIdleInSec))
                        .initialSize(initConnections)
                        .maxSize(maxConnections)
                        .name("kyc")
                        .registerJmx(registerJmx)
                        .build();

        return new ConnectionPool(poolConfiguration);
    }

    @Bean
    public DatabaseClient databaseClient(ConnectionPool connectionPool) {
        return DatabaseClient.builder()
                .connectionFactory(connectionPool)
                .build();
    }
}


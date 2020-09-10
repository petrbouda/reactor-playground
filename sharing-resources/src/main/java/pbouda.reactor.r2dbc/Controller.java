package pbouda.reactor.r2dbc;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.r2dbc.spi.Row;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@RestController
public class Controller {

    private final DatabaseClient databaseClient;

    public Controller(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @GetMapping(path = "persons")
    public Flux<Person> persons() {
        return databaseClient.execute("SELECT * FROM person")
                .map(Controller::toPerson)
                .all();
    }

    @GetMapping(path = "invoke")
    public Mono<HttpStatus> invoke() {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client -> client
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .doOnConnected(conn -> conn
                                .addHandlerLast(new ReadTimeoutHandler(10))
                                .addHandlerLast(new WriteTimeoutHandler(10))
                        )
                );

        WebClient webClient = WebClient.builder()
                .baseUrl("https://www.google.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

        return webClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMap(response -> Mono.just(response.statusCode()));
    }

    private static Person toPerson(Row row) {
        return new Person(
                row.get("personId", Integer.class),
                row.get("lastname", String.class),
                row.get("firstname", String.class));
    }

    public static class Person {
        private final Integer personId;
        private final String lastname;
        private final String firstname;

        public Person(Integer personId, String lastname, String firstname) {
            this.personId = personId;
            this.lastname = lastname;
            this.firstname = firstname;
        }

        public Integer getPersonId() {
            return personId;
        }

        public String getLastname() {
            return lastname;
        }

        public String getFirstname() {
            return firstname;
        }
    }
}

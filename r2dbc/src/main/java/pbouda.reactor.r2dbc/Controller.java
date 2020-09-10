package pbouda.reactor.r2dbc;

import io.r2dbc.spi.Row;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class Controller {

    private final DatabaseClient databaseClient;

    public Controller(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @GetMapping(path = "persons")
    public Flux<Person> allPersons() {
        return databaseClient.execute("SELECT * FROM person")
                .map(Controller::toPerson)
                .all();
    }

    private static Person toPerson(Row row) {
        return new Person(
                row.get("correlationId", Integer.class),
                row.get("personId", Integer.class),
                row.get("lastname", String.class),
                row.get("firstname", String.class),
                row.get("city", String.class),
                row.get("country", String.class));
    }
}

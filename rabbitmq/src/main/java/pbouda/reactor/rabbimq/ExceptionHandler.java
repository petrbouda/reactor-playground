package pbouda.reactor.rabbimq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class ExceptionHandler implements BiFunction<Throwable, ServerRequest, Mono<ServerResponse>> {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public Mono<ServerResponse> apply(Throwable throwable, ServerRequest request) {
        // It's an unmapped/unexpected error - log it for further investigation.
        LOG.error(throwable.getMessage());

        return ServerResponse.status(INTERNAL_SERVER_ERROR)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(new ErrorPayload(throwable.getMessage())), ErrorPayload.class);
    }
}

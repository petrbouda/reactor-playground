package pbouda.reactor.rabbimq;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class RequestHandler {

    private final RabbitSender rabbitSender;

    public RequestHandler(RabbitSender rabbitSender) {
        this.rabbitSender = rabbitSender;
    }

    @NonNull
    public Mono<ServerResponse> post(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Message.class)
                .map(message -> {
                    rabbitSender.send(message.getValue());
                    return "DONE";
                })
                .flatMap(RequestHandler::ok);
    }

    private static Mono<ServerResponse> ok(String response) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new Message(response)), Message.class);
    }
}

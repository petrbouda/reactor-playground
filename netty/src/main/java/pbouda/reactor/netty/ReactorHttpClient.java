package pbouda.reactor.netty;

import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;

public class ReactorHttpClient {

    public static void main(String[] args) {
        HttpClient.create()
                .port(8181)
                .post()
                .uri("/test/World")
                .send(ByteBufFlux.fromString(Flux.just("Hello")))
                .responseContent()
                .aggregate()
                .asString()
                .log("http-client")
                .block();
    }
}

package pbouda.reactor.netty;

import reactor.core.publisher.Flux;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReactorHttpClient {

    public static void main(String[] args) throws InterruptedException {
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.H2C)
                .port(1111);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable invocation = () -> {
            httpClient.post()
                    .uri("/test/World")
                    .send(ByteBufFlux.fromString(Flux.just("Hello")))
                    .responseContent()
                    .aggregate()
                    .asString()
                    .log("http-client")
                    .block();
        };

        scheduler.scheduleAtFixedRate(invocation, 0, 10, TimeUnit.SECONDS);

        Thread.currentThread().join();
    }
}

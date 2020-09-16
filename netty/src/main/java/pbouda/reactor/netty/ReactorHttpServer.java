package pbouda.reactor.netty;

import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServer;

public class ReactorHttpServer {

    public static void main(String[] args) {
        HttpServer.create()
                // .port(0)
                // Configures the port number as zero, this will let the system pick up
                // an ephemeral port when binding the server
                .port(8181)
                .route(routes -> {
                    routes.post("/test/{param}", (request, response) -> {
                        NettyOutbound out = response.sendString(request.receive()
                                .asString()
                                .map(s -> s + ' ' + request.param("param") + '!')
                                .log("http-server"));
                        return out;
                    });
                })
                .bindNow();
    }
}

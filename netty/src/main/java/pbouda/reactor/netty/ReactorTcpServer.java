package pbouda.reactor.netty;

import reactor.netty.tcp.TcpServer;

public class ReactorTcpServer {

    public static void main(String[] args) {
        TcpServer.create()
                .doOnBind(bootstrap -> System.out.println("Do on Bind"))
                .doOnBound(server -> System.out.println(server.host() + ":" + server.port()))
                .doOnUnbound(server -> System.out.println("Killing the server!"))
                .host("127.0.0.1")
                .port(1111)
                .bind()
                .block();
    }
}

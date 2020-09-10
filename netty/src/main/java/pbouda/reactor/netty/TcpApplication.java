package pbouda.reactor.netty;

import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;

public class TcpApplication {

    public static void main(String[] args) {
//        DisposableServer server =
//                TcpServer.create()
//                        .bindAddress()
//                        .handle((inbound, outbound) -> outbound.sendString(Mono.just("hello")))
//                        .bindNow();
//
//        server.onDispose()
//                .block();
    }
}

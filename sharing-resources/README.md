# Sharing a single EventLoopGroup among all Netty-based frameworks (servers and clients) in a App

### Defaults
```
public interface LoopResources extends Disposable {

	/**
	 * Default worker thread count, fallback to available processor (but with a minimum value of 4)
	 */
	int DEFAULT_IO_WORKER_COUNT = Integer.parseInt(System.getProperty(
			ReactorNetty.IO_WORKER_COUNT,
			"" + Math.max(Runtime.getRuntime()
			            .availableProcessors(), 4)));
	/**
	 * Default selector thread count, fallback to -1 (no selector thread)
	 */
	int DEFAULT_IO_SELECT_COUNT = Integer.parseInt(System.getProperty(
			ReactorNetty.IO_SELECT_COUNT,
			"" + -1));

	/**
	 * Default value whether the native transport (epoll, kqueue) will be preferred,
	 * fallback it will be preferred when available
	 */
	boolean DEFAULT_NATIVE = Boolean.parseBoolean(System.getProperty(
			ReactorNetty.NATIVE,
			"true"));
```

### SHARE EVERYTHING
- WebServer shares worker/select event-loop-group
- WebServer shares event-loop-group with R2DBC client
- Thanks to EPOLL event-loop-group can handle a plenty of connections
- Only one thread created.

run with `-Dreactor.netty.ioWorkerCount=1`

```
"reactor-tcp-epoll-1" #35 daemon prio=5 os_prio=0 cpu=3,35ms elapsed=22,90s tid=0x00007f704113d000 nid=0x52955 runnable  [0x00007f6fb6386000]
   java.lang.Thread.State: RUNNABLE
	at io.netty.channel.epoll.Native.epollWait(Native Method)
	at io.netty.channel.epoll.Native.epollWait(Native.java:148)
	at io.netty.channel.epoll.Native.epollWait(Native.java:141)
	at io.netty.channel.epoll.EpollEventLoop.epollWaitNoTimerChange(EpollEventLoop.java:290)
	at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:347)
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:989)
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.lang.Thread.run(java.base@14/Thread.java:832)

```
### How to re-use event-loop-groups from TCP Server and HTTP Server
- `reactor.netty.http.HttpResources`
- `reactor.netty.tcp.TcpResources`
- e.g. R2DBC can run on the same group as the HTTP server - fewer threads == less context-switching, 
cache efficiency

```
    public static void main(String[] args) {
        TcpResources tcpResources = TcpResources.get();
        HttpResources.set((LoopResources) tcpResources);

        SpringApplication application = new SpringApplication(Application.class);
        application.addInitializers(new CockroachInitializer());
        application.run(args);
    }
```

### WebServer
- Global Resources `reactor.netty.http.HttpResources`
- `org.springframework.boot.web.reactive.context.ReactiveWebServerApplicationContext.createWebServer`
- creates a WebServer instance and creates `ReactiveWebServerFactory` and `NettyReactiveWebServerFactory`

### WebClient 
- Global Resources `reactor.netty.http.HttpResources`

```
/*
By default, HttpClient participates in the global Reactor Netty resources
held in reactor.netty.http.HttpResources, including event loop threads
and a connection pool. This is the recommended mode, since fixed, shared
resources are preferred for event loop concurrency.
In this mode global resources remain active until the process exits.
If the server is timed with the process, there is typically no need
for an explicit shutdown. However, if the server can start or
stop in-process, you can declare a Spring-managed bean of type
ReactorResourceFactory with globalResources=true (the default)
to ensure that the Reactor Netty global resources are shut down
when the Spring ApplicationContext is closed, as the following
example shows:
*/

@Bean
 public ReactorResourceFactory reactorResourceFactory() {
     return new ReactorResourceFactory();
 }

/*
You can also choose not to participate in the global Reactor Netty resources.
However, in this mode, the burden is on you to ensure that all
Reactor Netty client and server instances use shared resources,
as the following example shows:
*/

@Bean
public ReactorResourceFactory resourceFactory() {
    ReactorResourceFactory factory = new ReactorResourceFactory();
    factory.setUseGlobalResources(false);
    return factory;
}

@Bean
public WebClient webClient() {
    Function<HttpClient, HttpClient> mapper = client -> {
        // Further customizations...
    };
    // Useful only when we want to use externally managed resources
    ClientHttpConnector connector =
            new ReactorClientHttpConnector(resourceFactory(), mapper);
    return WebClient.builder().clientConnector(connector).build();
}
```

### R2DBC 
- Global Resources `reactor.netty.tcp.TcpResources`
- `io.r2dbc.postgresql.client.ReactorNettyClient`

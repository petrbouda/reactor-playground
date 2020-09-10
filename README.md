# reactor-playground

### How to re-use event-loop-groups from TCP Server and HTTP Server
- `reactor.netty.http.HttpResources`
- `reactor.netty.tcp.TcpResources`
- e.g. R2DBC can run on the same group as the HTTP server - fewer threads == less context-switching

```
    public static void main(String[] args) {
        TcpResources tcpResources = TcpResources.get();
        HttpResources.set((LoopResources) tcpResources);

        SpringApplication application = new SpringApplication(Application.class);
        application.addInitializers(new CockroachInitializer());
        application.run(args);
    }
```


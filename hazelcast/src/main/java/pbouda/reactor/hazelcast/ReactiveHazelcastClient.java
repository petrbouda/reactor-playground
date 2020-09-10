package pbouda.reactor.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

public class ReactiveHazelcastClient {

    public static void main(String[] args) throws InterruptedException {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig()
                .addAddress("127.0.0.1");

        HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(HazelcastClient::shutdownAll));

        IMap<String, String> map = client.getMap("map");
        Mono.fromFuture(toCompletableFuture(map, "key", "value"))
                /**
                 * We never know what thread is actually used.
                 * -
                 * If a value of CompletableFuture is ready to be consumed
                 * before the Mono publisher calls "CompletableFuture#whenComplete"
                 * then it's called on a caller thread (in this case "main" thread)
                 * -
                 * If the ExecutionCallback is invoked after "#whenComplete"
                 * execution then the thread is the same as the thread that
                 * processed ExecutionCallback (in this case "hazelcast" thread)
                 *
                 * {@link java.util.concurrent.CompletableFuture#uniWhenCompleteStage}
                 * watch `result` value
                 */
                .subscribe(value -> {
                    System.out.println(value);
                });

        System.out.println("Waiting...");

        Thread.currentThread().join();
    }

    private static CompletableFuture<String> toCompletableFuture(IMap<String, String> map, String key, String value) {
        CompletableFuture<String> promise = new CompletableFuture<>();
        map.putAsync(key, value)
                .andThen(new ExecutionCallback<>() {
                    @Override
                    public void onResponse(String response) {
                        // Simulate a delay when hazelcast client fetches data

//                        try {
//                            Thread.sleep(5000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        promise.complete(response);
                    }

                    @Override
                    public void onFailure(Throwable ex) {
                        promise.completeExceptionally(ex);
                    }
                });
        return promise;
    }
}

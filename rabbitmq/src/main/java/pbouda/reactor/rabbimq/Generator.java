package pbouda.reactor.rabbimq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Generator {

    private static final Lorem LOREM = LoremIpsum.getInstance();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectNode EMPTY_NODE = MAPPER.createObjectNode();
    private static final URI ENDPOINT = URI.create("http://localhost:15672/api/exchanges/%2f/test/publish");

    public static void main(String[] args) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        for (int i = 0; i < 5000; i++) {
            invoke(httpClient, createRequest());
        }
    }

    private static HttpRequest createRequest() {
        ObjectNode objectNode = MAPPER.createObjectNode()
                .put("routing_key", "")
                .put("payload", LOREM.getFirstName() + " " + LOREM.getLastName())
                .put("payload_encoding", "string")
                .set("properties", EMPTY_NODE);

        return HttpRequest.newBuilder()
                .uri(ENDPOINT)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(objectNode.toString()))
                .build();
    }

    private static void invoke(HttpClient httpClient, HttpRequest request) {
        HttpResponse<Void> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        int statusCode = response.statusCode();
        if (errorResponse(statusCode)) {
            throw new RuntimeException("Status-code: " + statusCode);
        }
    }

    private static boolean errorResponse(int statusCode) {
        return !(statusCode >= 200 && statusCode <= 299);
    }
}

package pbouda.reactor.rabbitmq;

public class ErrorPayload {
   private final String message;

    public ErrorPayload(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

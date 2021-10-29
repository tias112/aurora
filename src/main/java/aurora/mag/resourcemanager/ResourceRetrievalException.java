package aurora.mag.resourcemanager;

public class ResourceRetrievalException extends ResourceException {
    public ResourceRetrievalException() {
    }

    public ResourceRetrievalException(String message) {
        super(message);
    }

    public ResourceRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}

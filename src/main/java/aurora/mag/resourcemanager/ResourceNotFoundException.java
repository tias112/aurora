package aurora.mag.resourcemanager;

public class ResourceNotFoundException extends ResourceException {
    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

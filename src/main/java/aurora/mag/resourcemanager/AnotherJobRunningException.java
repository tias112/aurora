package aurora.mag.resourcemanager;

public class AnotherJobRunningException extends RuntimeException {

    public AnotherJobRunningException(String message) {
        super(message);
    }
}

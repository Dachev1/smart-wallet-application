package app.exeptions;

public class DomainExceptions extends RuntimeException{
    public DomainExceptions(String message) {
        super(message);
    }

    public DomainExceptions(String message, Throwable cause) {
        super(message, cause);
    }
}

package sk.mlobb.be.rcon.model.exception;

public class BERconException extends RuntimeException {

    public BERconException() {
    }

    public BERconException(String message) {
        super(message);
    }

    public BERconException(String message, Throwable cause) {
        super(message, cause);
    }
}

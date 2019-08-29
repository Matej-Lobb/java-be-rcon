package sk.mlobb.be.rcon.model.exception;

/**
 * The type BattlEye Rcon exception.
 */
public class BERconException extends RuntimeException {

    /**
     * Instantiates a new BattlEye Rcon exception.
     *
     * @param message the message
     */
    public BERconException(String message) {
        super(message);
    }

    /**
     * Instantiates a new BattlEye Rcon exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public BERconException(String message, Throwable cause) {
        super(message, cause);
    }
}

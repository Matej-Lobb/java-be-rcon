package sk.mlobb.be.rcon.model.enums;

public enum BEDisconnectType {

    MANUAL("Manual disconnect"),
    CONNECTION_LOST("Connection lost (possible timeout)"),
    SOCKET_EXCEPTION("Connection lost due to exception");

    private final String message;

    private BEDisconnectType(String message) {
        this.message = message;
    }

    /**
     * Gets command.
     *
     * @return the command
     */
    public String getMessage() {
        return this.message;
    }
}
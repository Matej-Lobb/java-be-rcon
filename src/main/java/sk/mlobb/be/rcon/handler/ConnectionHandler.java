package sk.mlobb.be.rcon.handler;

import sk.mlobb.be.rcon.model.enums.BEConnectType;
import sk.mlobb.be.rcon.model.enums.BEDisconnectType;

/**
 * The interface Connection handler.
 */
public interface ConnectionHandler {
    /**
     * On connected.
     *
     * @param connectType the connect type
     */
    void onConnected(BEConnectType connectType);

    /**
     * On disconnected.
     *
     * @param disconnectType the disconnect type
     */
    void onDisconnected(BEDisconnectType disconnectType);
}

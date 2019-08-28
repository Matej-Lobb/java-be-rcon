package sk.mlobb.be.rcon;

import sk.mlobb.be.rcon.model.enums.BEConnectType;
import sk.mlobb.be.rcon.model.enums.BEDisconnectType;

public interface ConnectionHandler {
    void onConnected(BEConnectType connectType);
    void onDisconnected(BEDisconnectType disconnectType);
}

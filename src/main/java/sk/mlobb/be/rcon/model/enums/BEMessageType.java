package sk.mlobb.be.rcon.model.enums;

/**
 * The enum Be message type.
 */
public enum BEMessageType {
    /**
     * Login be message type.
     */
    LOGIN((byte) 0x00),
    /**
     * Command be message type.
     */
    COMMAND((byte) 0x01),
    /**
     * Server be message type.
     */
    SERVER((byte) 0x02),
    /**
     * Unknown be message type.
     */
    UNKNOWN((byte) 0xFF);

    private final byte type;

    BEMessageType(byte type) {
        this.type = type;
    }

    /**
     * Convert byte to packet type be message type.
     *
     * @param byteToConvert the byte to convert
     * @return the be message type
     */
    public static BEMessageType convertByteToPacketType(byte byteToConvert){
        BEMessageType packetType;
        switch (byteToConvert){
            case 0x00:
                packetType = LOGIN;
                break;
            case 0x01:
                packetType = COMMAND;
                break;
            case 0x02:
                packetType = SERVER;
                break;
            default:
                packetType = UNKNOWN;
                break;
        }

        return packetType;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public byte getType() {
        return type;
    }
}

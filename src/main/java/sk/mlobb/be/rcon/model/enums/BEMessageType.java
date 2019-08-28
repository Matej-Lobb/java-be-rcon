package sk.mlobb.be.rcon.model.enums;

/**
 * The enum Be message type.
 */
public enum BEMessageType {
    /**
     * Login be message type.
     */
    Login((byte) 0x00),
    /**
     * Command be message type.
     */
    Command((byte) 0x01),
    /**
     * Server be message type.
     */
    Server((byte) 0x02),
    /**
     * Unknown be message type.
     */
    Unknown((byte) 0xFF);

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
                packetType = Login;
                break;
            case 0x01:
                packetType = Command;
                break;
            case 0x02:
                packetType = Server;
                break;
            default:
                packetType = Unknown;
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

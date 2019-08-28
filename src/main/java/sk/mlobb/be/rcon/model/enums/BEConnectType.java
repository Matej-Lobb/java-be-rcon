package sk.mlobb.be.rcon.model.enums;

/**
 * The enum BattlEye Rcon connect type.
 */
public enum BEConnectType {
    /**
     * Failure BattlEye Rcon connect type.
     */
    Failure(),
    /**
     * Success BattlEye Rcon connect type.
     */
    Success(),
    /**
     * Unknown BattlEye Rcon connect type.
     */
    Unknown();

    BEConnectType() {
    }

    /**
     * Convert byte to connect type be connect type.
     *
     * @param byteToConvert the byte to convert
     * @return the be connect type
     */
    public static BEConnectType convertByteToConnectType(byte byteToConvert){
        BEConnectType packetType;
        switch (byteToConvert){
            case 0x00:
                packetType = Failure;
                break;
            case 0x01:
                packetType = Success;
                break;
            default:
                packetType = Unknown;
                break;
        }

        return packetType;
    }
}

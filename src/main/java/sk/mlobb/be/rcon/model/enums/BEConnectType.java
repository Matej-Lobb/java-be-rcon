package sk.mlobb.be.rcon.model.enums;

/**
 * The enum BattlEye Rcon connect type.
 */
public enum BEConnectType {
    /**
     * Failure BattlEye Rcon connect type.
     */
    FAILURE(),
    /**
     * Success BattlEye Rcon connect type.
     */
    SUCCESS(),
    /**
     * Unknown BattlEye Rcon connect type.
     */
    UNKNOWN();

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
                packetType = FAILURE;
                break;
            case 0x01:
                packetType = SUCCESS;
                break;
            default:
                packetType = UNKNOWN;
                break;
        }

        return packetType;
    }
}

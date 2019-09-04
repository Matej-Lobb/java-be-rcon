package sk.mlobb.be.rcon.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * The type BattlEye Rcon configuration.
 */
@Data
@Builder
@AllArgsConstructor
public class BERconConfiguration {

    /**
     * With Default values
     */
    private Long keepAliveTime;
    private Long connectionDelay;
    private Long timeoutTime;

    /**
     * Instantiates a new Be rcon configuration.
     */
    public BERconConfiguration() {
        this.keepAliveTime = 27000L;
        this.connectionDelay = 1000L;
        this.timeoutTime = 10000L;
    }
}

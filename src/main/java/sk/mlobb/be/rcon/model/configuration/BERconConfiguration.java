package sk.mlobb.be.rcon.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type BattlEye Rcon configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BERconConfiguration {

    /**
     * Required
     */
    private String ip;
    private Integer port;
    private String password;

    /**
     * With Default values
     */
    private Long keepAliveTime = 27000L;
    private Long connectionDelay = 1000L;
    private Long timeoutTime = 10000L;
}

package sk.mlobb.be.rcon.model.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BERconConfiguration {

    private String ip;
    private Integer port;
    private String password;
    private Long keepAliveTime;
    private Long connectionDelay;
    private Long timeoutTime;
}

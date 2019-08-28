package sk.mlobb.be.rcon.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetSocketAddress;

/**
 * The type Be login credential.
 */
@Data
@AllArgsConstructor
public class BELoginCredential {

    /**
     * The Host address.
     */
    public InetSocketAddress hostAddress;
    /**
     * The Host password.
     */
    public String hostPassword;
}

package sk.mlobb.be.rcon.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetSocketAddress;

@Data
@AllArgsConstructor
public class BELoginCredential {

    public InetSocketAddress hostAddress;
    public String hostPassword;
}

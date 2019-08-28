package sk.mlobb.be.rcon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import sk.mlobb.be.rcon.model.enums.BEMessageType;

@Data
@AllArgsConstructor
public class BECommand {

    private BEMessageType messageType;
    private String command;
}

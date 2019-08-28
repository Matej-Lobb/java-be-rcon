package sk.mlobb.be.rcon.model.command;

/**
 * The enum Dayz be command type.
 */
public enum DayzBECommandType implements BECommandType {

    /**
     * Reload server config file loaded by –config option
     */
    INIT("#init"),
    /**
     * 	Shuts down the server
     */
    SHUTDOWN("#shutdown"),
    /**
     * Start over and reassign roles
     */
    REASSIGN("#reassign"),
    /**
     * Restart mission
     */
    RESTART("#restart"),
    /**
     * Lock server
     */
    LOCK("#lock"),
    /**
     * Unlock server.
     */
    UNLOCK("#unlock"),
    /**
     * Show Mission
     */
    MISSION("#mission "),
    /**
     * Show Missions.
     */
    MISSIONS("missions"),
    /**
     * Rcon password.
     */
    RCON_PASSWORD("rconpassword"),
    /**
     * Max ping.
     */
    MAX_PING("maxping"),
    /**
     * Kick.
     */
    KICK("kick"),
    /**
     * Show Players.
     */
    PLAYERS("players"),
    /**
     * Say.
     */
    SAY("say "),
    /**
     * Load bans.
     */
    LOAD_BANS("loadbans"),
    /**
     * Load scripts.
     */
    LOAD_SCRIPTS("loadScripts"),
    /**
     * Load events.
     */
    LOAD_EVENTS("loadevents"),
    /**
     * Show Bans.
     */
    BANS("bans"),
    /**
     * Ban.
     */
    BAN("ban"),
    /**
     * Add Ban.
     */
    ADD_BAN("addban"),
    /**
     * Remove Ban.
     */
    REMOVE_BAN("removeban"),
    /**
     * Write Bans.
     */
    WRITE_BANS("writebans"),
    /**
     * Admins.
     */
    ADMINS("admins"),
    /**
     * Empty.
     */
    EMPTY("");

    private final String value;

    private DayzBECommandType(String value) {
        this.value = value;
    }

    /**
     * Gets command.
     *
     * @return the command
     */
    public String getCommand() {
        return this.value;
    }
}

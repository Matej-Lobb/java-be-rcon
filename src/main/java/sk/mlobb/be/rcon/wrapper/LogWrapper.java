package sk.mlobb.be.rcon.wrapper;

public interface LogWrapper {

    void debug(String msg);
    void info(String msg);

    void warn(String msg);
    void warn(String msg, Throwable t);
}

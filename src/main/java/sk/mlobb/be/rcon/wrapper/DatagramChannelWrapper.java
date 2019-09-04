package sk.mlobb.be.rcon.wrapper;

import java.io.IOException;
import java.nio.channels.DatagramChannel;

/**
 * The type Datagram channel wrapper.
 */
public class DatagramChannelWrapper {

    /**
     * Open datagram channel.
     *
     * @return the datagram channel
     * @throws IOException the io exception
     */
    public DatagramChannel open() throws IOException {
        return DatagramChannel.open();
    }

    /**
     * Close.
     *
     * @param datagramChannel the datagram channel
     * @throws IOException the io exception
     */
    public void close(DatagramChannel datagramChannel) throws IOException {
        datagramChannel.close();
    }
}

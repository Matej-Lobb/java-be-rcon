package sk.mlobb.be.rcon;

import lombok.Getter;
import sk.mlobb.be.rcon.handler.ConnectionHandler;
import sk.mlobb.be.rcon.handler.ResponseHandler;
import sk.mlobb.be.rcon.model.BECommand;
import sk.mlobb.be.rcon.model.BELoginCredential;
import sk.mlobb.be.rcon.model.enums.LoggingLevel;
import sk.mlobb.be.rcon.model.command.BECommandType;
import sk.mlobb.be.rcon.model.configuration.BERconConfiguration;
import sk.mlobb.be.rcon.model.enums.BEConnectType;
import sk.mlobb.be.rcon.model.enums.BEDisconnectType;
import sk.mlobb.be.rcon.model.enums.BEMessageType;
import sk.mlobb.be.rcon.model.exception.BERconException;
import sk.mlobb.be.rcon.wrapper.DatagramChannelWrapper;
import sk.mlobb.be.rcon.wrapper.LogWrapper;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

/**
 * The type BattlEye Rcon client.
 */
public class BERconClient {

    private final LogWrapper log;

    @Getter
    private final AtomicBoolean connected;
    @Getter
    private Thread monitorThread;
    @Getter
    private Thread receiveThread;

    private final List<ConnectionHandler> connectionHandlerList;
    private final BERconConfiguration beRconConfiguration;
    private final List<ResponseHandler> responseHandlerList;
    private final CRC32 crc32;

    private DatagramChannelWrapper datagramChannelWrapper;
    private DatagramChannel datagramChannel;
    private Queue<BECommand> commandQueue;
    private AtomicInteger sequenceNumber;
    private AtomicLong lastReceivedTime;
    private ByteBuffer receiveBuffer;
    private AtomicLong lastSentTime;
    private ByteBuffer sendBuffer;
    private Boolean loggingEnabled = false;

    /**
     * Instantiates a new BattlEye Rcon client.
     *
     * @param beRconConfiguration the be rcon configuration
     */
    public BERconClient(BERconConfiguration beRconConfiguration) {
        this(beRconConfiguration, null);
    }

    /**
     * Instantiates a new BattlEye Rcon client.
     *
     * @param beRconConfiguration the be rcon configuration
     * @param log                 the log
     */
    public BERconClient(BERconConfiguration beRconConfiguration, LogWrapper log) {
        this.beRconConfiguration = beRconConfiguration;
        this.log = log;
        if (log != null) {
            loggingEnabled = true;
        }
        crc32 = new CRC32();
        connected = new AtomicBoolean(false);
        connectionHandlerList = new ArrayList<>();
        responseHandlerList = new ArrayList<>();
        receiveThread = new Thread(getReceiveRunnable());
        receiveThread.setName("rcdata-1");
        monitorThread = new Thread(getMonitorRunnable());
        monitorThread.setName("rcmonitor-1");
        datagramChannelWrapper = new DatagramChannelWrapper();
    }

    /**
     * Connect to BattlEye Rcon.
     *
     * @param beLoginCredential the be login credential
     * @throws IOException the io exception
     */
    public void connect(BELoginCredential beLoginCredential) throws IOException {
        logIfEnabled("Connecting to Rcon ...", LoggingLevel.INFO);
        datagramChannel = datagramChannelWrapper.open();
        datagramChannel.connect(beLoginCredential.getHostAddress());
        logIfEnabled("Datagram connected ...", LoggingLevel.DEBUG);

        sendBuffer = ByteBuffer.allocate(datagramChannel.getOption(StandardSocketOptions.SO_SNDBUF));
        sendBuffer.order(ByteOrder.LITTLE_ENDIAN);

        receiveBuffer = ByteBuffer.allocate(datagramChannel.getOption(StandardSocketOptions.SO_RCVBUF));
        receiveBuffer.order(ByteOrder.LITTLE_ENDIAN);

        commandQueue = new ConcurrentLinkedDeque<>();

        lastSentTime = new AtomicLong(System.currentTimeMillis());
        lastReceivedTime = new AtomicLong(System.currentTimeMillis());
        sequenceNumber = new AtomicInteger(-1);

        try {
            logIfEnabled(String.format("Starting thread: %s", receiveThread.getName()), LoggingLevel.DEBUG);
            receiveThread.start();
            logIfEnabled(String.format("Starting thread: %s", monitorThread.getName()), LoggingLevel.DEBUG);
            monitorThread.start();
        } catch (Exception e) {
            throw new BERconException(String.format("Failed to receive/monitor BattlEye Rcon! Most probably is not " +
                    "running in: %s", beLoginCredential.getHostAddress()));
        }

        logIfEnabled("Perform login ...", LoggingLevel.INFO);
        constructPacket(BEMessageType.LOGIN, -1, beLoginCredential.getHostPassword());
        sendData();
    }

    /**
     * Send command.
     *
     * @param commands the commands
     */
    public void sendCommand(String... commands) {
        final StringBuilder commandBuilder = new StringBuilder();
        for (String command : commands) {
            commandBuilder.append(' ');
            commandBuilder.append(command);
        }

        addCommandToQueue(new BECommand(BEMessageType.COMMAND, commandBuilder.toString()));
    }


    /**
     * Send command.
     *
     * @param commandType the command type
     */
    public void sendCommand(BECommandType commandType) {
        addCommandToQueue(new BECommand(BEMessageType.COMMAND, commandType.getCommand()));
    }

    /**
     * Send command.
     *
     * @param commandType the command type
     * @param commandArgs the command args
     */
    public void sendCommand(BECommandType commandType, String... commandArgs) {
        final StringBuilder commandBuilder = new StringBuilder(commandType.getCommand());
        for (String arg : commandArgs) {
            commandBuilder.append(' ');
            commandBuilder.append(arg);
        }
        logIfEnabled(String.format("Sending command: %s", commandBuilder.toString()), LoggingLevel.DEBUG);
        addCommandToQueue(new BECommand(BEMessageType.COMMAND, commandBuilder.toString()));
    }

    /**
     * Disconnect.
     */
    public void disconnect() {
        disconnect(BEDisconnectType.MANUAL);
    }

    private void disconnect(BEDisconnectType disconnectType) {
        try {
            logIfEnabled("Disconnecting ...", LoggingLevel.INFO);
            connected.set(false);
            commandQueue = null;
            datagramChannel.disconnect();
            datagramChannelWrapper.close(datagramChannel);
            receiveThread.interrupt();
            monitorThread.interrupt();
            receiveThread = null;
            sendBuffer = null;
            receiveBuffer = null;
            fireConnectionDisconnectVent(disconnectType);
        } catch (IOException e) {
            logIfEnabled("Failed to disconnect !", LoggingLevel.WARNING, e);
        }
    }

    /**
     * Construct a packet following the BattlEye protocol
     * http://www.battleye.com/downloads/BERConProtocol.txt
     */
    private void constructPacket(BEMessageType messageType, int sequenceNumber, String command) {
        logIfEnabled("Constructing packet ...", LoggingLevel.DEBUG);
        sendBuffer.clear();
        sendBuffer.put((byte) 'B');
        sendBuffer.put((byte) 'E');
        sendBuffer.position(6);
        sendBuffer.put((byte) 0xFF);
        sendBuffer.put(messageType.getType());

        if (sequenceNumber >= 0) {
            sendBuffer.put((byte) sequenceNumber);
        }

        if (command != null && !command.isEmpty()) {
            sendBuffer.put(command.getBytes());
        }

        crc32.reset();
        crc32.update(sendBuffer.array(), 6, sendBuffer.position() - 6);
        sendBuffer.putInt(2, (int) crc32.getValue());

        sendBuffer.flip();
    }

    private void sendData() {
        logIfEnabled("Sending data ...", LoggingLevel.DEBUG);
        if (datagramChannel.isConnected()) {
            try {
                datagramChannel.write(sendBuffer);
                lastSentTime.set(System.currentTimeMillis());
                Thread.sleep(beRconConfiguration.getConnectionDelay());
            } catch (IOException e) {
                throw new BERconException("Failed to send data !", e);
            } catch (InterruptedException e) {
                logIfEnabled("Interrupted !", LoggingLevel.WARNING, e);
                Thread.currentThread().interrupt();
            }
        }
    }

    void sendNextCommand() {
        logIfEnabled("Sending next command ...", LoggingLevel.DEBUG);
        if (commandQueue != null && !commandQueue.isEmpty()) {
            final BECommand beCommand = commandQueue.poll();
            if (beCommand != null) {
                constructPacket(beCommand.getMessageType(), nextSequenceNumber(), beCommand.getCommand());
                sendData();
            }
        }
    }

    /**
     * Receives and validates the incoming packet
     * 'B'(0x42) | 'E'(0x45) | 4-byte CRC32 checksum of the subsequent bytes | 0xFF
     */
    private boolean receiveData() throws IOException {
        logIfEnabled("Receiving data ...", LoggingLevel.DEBUG);
        receiveBuffer.clear();
        int read = datagramChannel.read(receiveBuffer);
        if (read < 7) {
            return false;
        }

        receiveBuffer.flip();
        if (receiveBuffer.get() != (byte) 'B' || receiveBuffer.get() != (byte) 'E') {
            return false;
        }

        receiveBuffer.getInt();
        return receiveBuffer.get() == (byte) 0xFF;
    }

    private int nextSequenceNumber() {
        int tempSequenceNumber = sequenceNumber.get();
        tempSequenceNumber = tempSequenceNumber == 255 ? 0 : tempSequenceNumber + 1;
        sequenceNumber.set(tempSequenceNumber);
        return sequenceNumber.get();
    }

    private void addCommandToQueue(BECommand command) {
        logIfEnabled(String.format("Adding command to queue: %s", command.toString()), LoggingLevel.DEBUG);
        if (!commandQueue.isEmpty()) {
            commandQueue.add(command);
        } else {
            commandQueue.add(command);
            sendNextCommand();
        }
    }

    /**
     * Add connection handler.
     *
     * @param connectionHandler the connection handler
     */
    public void addConnectionHandler(ConnectionHandler connectionHandler) {
        connectionHandlerList.add(connectionHandler);
    }

    /**
     * Add response handler.
     *
     * @param responseHandler the response handler
     */
    public void addResponseHandler(ResponseHandler responseHandler) {
        responseHandlerList.add(responseHandler);
    }

    private void fireConnectionConnectedEvent(BEConnectType connectType) {
        for (ConnectionHandler connectionHandler : connectionHandlerList) {
            connectionHandler.onConnected(connectType);
        }
    }

    private void fireConnectionDisconnectVent(BEDisconnectType disconnectType) {
        for (ConnectionHandler connectionHandler : connectionHandlerList) {
            connectionHandler.onDisconnected(disconnectType);
        }
    }

    private void fireResponseEvent(String response) {
        for (ResponseHandler responseHandler : responseHandlerList) {
            responseHandler.onResponse(response);
        }
    }

    private Runnable getReceiveRunnable() {
        return () -> {
            try {
                while (datagramChannel.isConnected()) {
                    if (receiveData()) {
                        lastReceivedTime.set(System.currentTimeMillis());
                        BEMessageType messageType = BEMessageType.convertByteToPacketType(receiveBuffer.get());
                        switch (messageType) {
                            case LOGIN:
                                receiveLoginPacket();
                                break;
                            case COMMAND:
                                receiveCommandPacket();
                                break;
                            case SERVER:
                                receiveServerPacket();
                                break;
                            case UNKNOWN:
                                logIfEnabled("Received unknown packet!", LoggingLevel.WARNING);
                                break;
                            default: {
                                logIfEnabled("Unknown packet", LoggingLevel.WARNING);
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new BERconException("Receiving failed !", e);
            }
        };
    }

    private void receiveServerPacket() {
        byte serverSequenceNumber = receiveBuffer.get();
        fireResponseEvent(new String(receiveBuffer.array(), receiveBuffer.position(), receiveBuffer.remaining()));
        constructPacket(BEMessageType.SERVER, serverSequenceNumber, null);
        sendData();
        sendNextCommand();
    }

    private void receiveCommandPacket() throws IOException {
        receiveBuffer.get();
        if (receiveBuffer.hasRemaining()) {
            if (receiveBuffer.get() == 0x00) {
                int totalPackets = receiveBuffer.get();
                int packetIndex = receiveBuffer.get();
                String[] messageArray = new String[totalPackets];
                messageArray[packetIndex] = new String(receiveBuffer.array(), receiveBuffer.position(), receiveBuffer.remaining());
                packetIndex++;

                while (packetIndex < totalPackets) {
                    receiveData();
                    receiveBuffer.position(12);
                    messageArray[packetIndex] = new String(receiveBuffer.array(), receiveBuffer.position(), receiveBuffer.remaining());
                    packetIndex++;
                }

                StringBuilder completeMessage = new StringBuilder();
                for (String message : messageArray) {
                    completeMessage.append(message);
                }

                fireResponseEvent(completeMessage.toString());
            } else {
                receiveBuffer.position(receiveBuffer.position() - 1);
                fireResponseEvent(new String(receiveBuffer.array(), receiveBuffer.position(), receiveBuffer.remaining()));
            }
            sendNextCommand();
        }
    }

    private void receiveLoginPacket() {
        try {
            BEConnectType connectionResult = BEConnectType.convertByteToConnectType(receiveBuffer.array()[8]);
            switch (connectionResult) {
                case FAILURE:
                    fireConnectionConnectedEvent(BEConnectType.FAILURE);
                    disconnect(BEDisconnectType.CONNECTION_LOST);
                    break;
                case SUCCESS:
                    fireConnectionConnectedEvent(BEConnectType.SUCCESS);
                    connected.set(true);
                    break;
                case UNKNOWN:
                    fireConnectionConnectedEvent(BEConnectType.UNKNOWN);
                    disconnect(BEDisconnectType.SOCKET_EXCEPTION);
                    break;
                default:
                    logIfEnabled("Invalid connection result!", LoggingLevel.WARNING);
                    break;
            }
        } catch (Exception e) {
            throw new BERconException("Failed to login!", e);
        }
    }

    private Runnable getMonitorRunnable() {
        return () -> {
            while (datagramChannel.isConnected()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    logIfEnabled(String.format("Last received packet time: %s", lastReceivedTime.get()),
                            LoggingLevel.DEBUG);
                    logIfEnabled(String.format("Last sent packet time: %s", lastSentTime.get()), LoggingLevel.DEBUG);
                    checkTimeout();
                    sentKeepAlive();
                } catch (InterruptedException e) {
                    logIfEnabled("Interrupted !", LoggingLevel.WARNING, e);
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    void checkTimeout() {
        logIfEnabled("Checking timeout ...", LoggingLevel.DEBUG);
        if (lastSentTime.get() - lastReceivedTime.get() > beRconConfiguration.getTimeoutTime()) {
            disconnect(BEDisconnectType.CONNECTION_LOST);
        }
    }

    void sentKeepAlive() {
        logIfEnabled("Check if keep alive is needed ...", LoggingLevel.DEBUG);
        if (System.currentTimeMillis() - lastSentTime.get() >= beRconConfiguration.getKeepAliveTime()) {
            constructPacket(BEMessageType.COMMAND, nextSequenceNumber(), null);
            sendData();
            logIfEnabled("Sent empty packet for keep alive!", LoggingLevel.INFO);
        }
    }

    private void logIfEnabled(String message, LoggingLevel level) {
        logIfEnabled(message, level, null);
    }

    private void logIfEnabled(String message, LoggingLevel level, Throwable throwable) {
        if (loggingEnabled) {
            switch (level) {
                case DEBUG: {
                    debugLog(message, throwable);
                    break;
                }
                case INFO: {
                    infoLog(message, throwable);
                    break;
                }
                case WARNING: {
                    warnLog(message, throwable);
                    break;
                }
            }
        }
    }

    private void warnLog(String message, Throwable throwable) {
        if (throwable == null) {
            log.warn(message);
            return;
        }
        log.warn(message, throwable);
    }

    private void infoLog(String message, Throwable throwable) {
        if (throwable == null) {
            log.info(message);
            return;
        }
        log.info(message, throwable);
    }

    private void debugLog(String message, Throwable throwable) {
        if (throwable == null) {
            log.debug(message);
            return;
        }
        log.debug(message, throwable);
    }
}

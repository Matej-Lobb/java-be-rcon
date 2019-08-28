package sk.mlobb.be.rcon;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sk.mlobb.be.rcon.model.BECommand;
import sk.mlobb.be.rcon.model.BELoginCredential;
import sk.mlobb.be.rcon.model.configuration.BERconConfiguration;
import sk.mlobb.be.rcon.model.enums.BECommandType;
import sk.mlobb.be.rcon.model.enums.BEConnectType;
import sk.mlobb.be.rcon.model.enums.BEDisconnectType;
import sk.mlobb.be.rcon.model.enums.BEMessageType;
import sk.mlobb.be.rcon.model.exception.BERconException;

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

@Slf4j
public class BERconClient {

    @Getter
    private final AtomicBoolean connected;
    @Getter
    private Thread monitorThread;
    @Getter
    private Thread receiveThread;

    private final List<ConnectionHandler> connectionHandlerList;
    private final BERconConfiguration dayzYamlConfiguration;
    private final List<ResponseHandler> responseHandlerList;
    private final CRC32 crc32;

    private DatagramChannel datagramChannel;
    private Queue<BECommand> commandQueue;
    private AtomicInteger sequenceNumber;
    private AtomicLong lastReceivedTime;
    private ByteBuffer receiveBuffer;
    private AtomicLong lastSentTime;
    private ByteBuffer sendBuffer;

    /**
     * Instantiates a new BattleEye rcon client.
     */
    public BERconClient(BERconConfiguration dayzYamlConfiguration) {
        this.dayzYamlConfiguration = dayzYamlConfiguration;
        crc32 = new CRC32();
        connected = new AtomicBoolean(false);
        connectionHandlerList = new ArrayList<>();
        responseHandlerList = new ArrayList<>();
        receiveThread = new Thread(getReceiveRunnable());
        receiveThread.setName("rcdata-1");
        monitorThread = new Thread(getMonitorRunnable());
        monitorThread.setName("rcmonitor-1");
    }

    /**
     * Connect to Rcon.
     *
     * @param beLoginCredential the be login credential
     * @throws IOException the io exception
     */
    public void connect(BELoginCredential beLoginCredential) throws IOException {
        log.info("Connecting to Rcon ...");
        datagramChannel = DatagramChannel.open();
        datagramChannel.connect(beLoginCredential.getHostAddress());
        log.debug("Datagram connected ...");

        sendBuffer = ByteBuffer.allocate(datagramChannel.getOption(StandardSocketOptions.SO_SNDBUF));
        sendBuffer.order(ByteOrder.LITTLE_ENDIAN);

        receiveBuffer = ByteBuffer.allocate(datagramChannel.getOption(StandardSocketOptions.SO_RCVBUF));
        receiveBuffer.order(ByteOrder.LITTLE_ENDIAN);

        commandQueue = new ConcurrentLinkedDeque<>();

        lastSentTime = new AtomicLong(System.currentTimeMillis());
        lastReceivedTime = new AtomicLong(System.currentTimeMillis());
        sequenceNumber = new AtomicInteger(-1);

        log.debug("Starting thread: {}", receiveThread.getName());
        receiveThread.start();
        log.debug("Starting thread: {}", monitorThread.getName());
        monitorThread.start();

        // Login packet is a bit unique since we want to set -1 as the sequence number
        log.info("Perform login ...");
        constructPacket(BEMessageType.Login, -1, beLoginCredential.getHostPassword());
        sendData();
    }

    /**
     * Send command.
     *
     * @param commandType the command type
     */
    public void sendCommand(BECommandType commandType) {
        addCommandToQueue(new BECommand(BEMessageType.Command, commandType.toString()));
    }

    /**
     * Send command.
     *
     * @param commandType the command type
     * @param commandArgs the command args
     */
    public void sendCommand(BECommandType commandType, String... commandArgs) {
        final StringBuilder commandBuilder = new StringBuilder(commandType.toString());
        for (String arg : commandArgs) {
            commandBuilder.append(' ');
            commandBuilder.append(arg);
        }
        log.debug("Sending command: {}", commandBuilder.toString());
        addCommandToQueue(new BECommand(BEMessageType.Command, commandBuilder.toString()));
    }

    private void disconnect(BEDisconnectType disconnectType) {
        try {
            log.debug("Disconnecting ...");
            connected.set(false);
            commandQueue = null;
            datagramChannel.disconnect();
            datagramChannel.close();
            receiveThread.interrupt();
            monitorThread.interrupt();
            receiveThread = null;
            sendBuffer = null;
            receiveBuffer = null;
            fireConnectionDisconnectVent(disconnectType);
        } catch (IOException e) {
            log.warn("Failed to disconnect !", e);
        }
    }

    /**
     * Construct a packet following the BE protocol
     * http://www.battleye.com/downloads/BERConProtocol.txt
     */
    private void constructPacket(BEMessageType messageType, int sequenceNumber, String command) {
        log.debug("Constructing packet ...");
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
        log.debug("Sending data ...");
        if (datagramChannel.isConnected()) {
            try {
                datagramChannel.write(sendBuffer);
                lastSentTime.set(System.currentTimeMillis());
                Thread.sleep(dayzYamlConfiguration.getConnectionDelay());
            } catch (IOException e) {
                throw new BERconException("Failed to send data !", e);
            } catch (InterruptedException e) {
                log.warn("Interrupted !", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sendNextCommand() {
        log.debug("Sending next command ...");
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
        log.debug("Receiving data ...");
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
        log.debug("Adding command to queue: {}", command.toString());
        if (!commandQueue.isEmpty()) {
            commandQueue.add(command);
        } else {
            commandQueue.add(command);
            sendNextCommand();
        }
    }

    public void addConnectionHandler(ConnectionHandler connectionHandler) {
        connectionHandlerList.add(connectionHandler);
    }

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
                            case Login: {
                                receiveLoginPacket();
                            }
                            break;
                            case Command: {
                                receiveCommandPacket();
                            }
                            break;
                            case Server: {
                                receiveServerPacket();
                            }
                            break;
                            case Unknown: {
                                log.warn("Received unknown packet!");
                            }
                            break;
                            default: {
                                log.info("Unknown packet");
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
        //Output the message and send an acknowledgement to the server
        byte serverSequenceNumber = receiveBuffer.get();
        fireResponseEvent(new String(receiveBuffer.array(), receiveBuffer.position(), receiveBuffer.remaining()));
        constructPacket(BEMessageType.Server, serverSequenceNumber, null);
        sendData();
        sendNextCommand();
    }

    private void receiveCommandPacket() throws IOException {
        //Check to see if this message is segmented
        receiveBuffer.get();
        //Check to prevent BufferUnderFlowException
        if (receiveBuffer.hasRemaining()) {
            if (receiveBuffer.get() == 0x00) {
                int totalPackets = receiveBuffer.get();
                int packetIndex = receiveBuffer.get();
                String[] messageArray = new String[totalPackets];
                messageArray[packetIndex] = new String(receiveBuffer.array(), receiveBuffer.position(), receiveBuffer.remaining());
                packetIndex++;

                //Process the remaining segmented messages
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
                case Failure:
                    fireConnectionConnectedEvent(BEConnectType.Failure);
                    disconnect(BEDisconnectType.ConnectionLost);
                    break;
                case Success:
                    fireConnectionConnectedEvent(BEConnectType.Success);
                    connected.set(true);
                    break;
                case Unknown:
                    fireConnectionConnectedEvent(BEConnectType.Unknown);
                    disconnect(BEDisconnectType.ConnectionLost);
                    break;
                default:
                    log.warn("Invalid connection result!");
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
                    log.debug("Last received packet time: {}", lastReceivedTime.get());
                    log.debug("Last sent packet time: {}", lastSentTime.get());
                    checkTimeout();
                    sentKeepAlive();
                } catch (InterruptedException e) {
                    log.warn("Interrupted !", e);
                    Thread.currentThread().interrupt();
                }
            }
        };
    }

    private void checkTimeout() {
        log.debug("Checking timeout ...");
        if (lastSentTime.get() - lastReceivedTime.get() > dayzYamlConfiguration.getTimeoutTime()) {
            disconnect(BEDisconnectType.ConnectionLost);
        }
    }

    private void sentKeepAlive() {
        log.debug("Check if keep alive is needed ...");
        if (System.currentTimeMillis() - lastSentTime.get() >= dayzYamlConfiguration.getKeepAliveTime()) {
            constructPacket(BEMessageType.Command, nextSequenceNumber(), null);
            sendData();
            log.info("Sent empty packet for keep alive!");
        }
    }
}

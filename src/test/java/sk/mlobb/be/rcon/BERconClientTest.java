package sk.mlobb.be.rcon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.mlobb.be.rcon.handler.ConnectionHandler;
import sk.mlobb.be.rcon.handler.ResponseHandler;
import sk.mlobb.be.rcon.model.BECommand;
import sk.mlobb.be.rcon.model.BELoginCredential;
import sk.mlobb.be.rcon.model.command.DayzBECommandType;
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
import java.nio.channels.DatagramChannel;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BERconClientTest {

    private BERconClient beRconClient;

    @Mock
    private BERconConfiguration configuration;

    @Mock
    private DatagramChannel datagramChannel;

    @Mock
    private Queue<BECommand> commandQueue;

    @Mock
    private BECommand beCommand;

    @Mock
    private DatagramChannelWrapper datagramChannelWrapper;

    @Mock
    private BELoginCredential beLoginCredential;

    @BeforeEach
    void setUp() throws IllegalAccessException {
        AtomicInteger sequenceNumber = new AtomicInteger(0);
        ByteBuffer receiveBuffer = ByteBuffer.wrap("test".getBytes());
        AtomicLong lastSentTime = new AtomicLong(100L);
        AtomicLong lastReceivedTime = new AtomicLong(0);
        ByteBuffer sendBuffer = ByteBuffer.allocate(9999);
        configuration.setConnectionDelay(1L);
        beRconClient = new BERconClient(configuration, new TestLogImpl());

        writeField(beRconClient, "datagramChannelWrapper", datagramChannelWrapper, true);
        writeField(beRconClient, "sequenceNumber", sequenceNumber , true);
        writeField(beRconClient, "commandQueue", commandQueue, true);
        writeField(beRconClient, "receiveBuffer", receiveBuffer, true);
        writeField(beRconClient, "sendBuffer", sendBuffer, true);
        writeField(beRconClient, "datagramChannel", datagramChannel, true);
        writeField(beRconClient, "lastSentTime", lastSentTime, true);
        writeField(beRconClient, "lastReceivedTime", lastReceivedTime ,true);
        writeField(beRconClient, "beRconConfiguration", configuration, true);
        writeField(beRconClient, "log", new TestLogImpl(), true);
        writeField(beRconClient, "loggingEnabled", true, true);

        beRconClient.addConnectionHandler(new CustomConnectionHandler());
        beRconClient.addResponseHandler(new CustomResponseHandler());
    }

    @Test
    void shouldReceiveLoginPacketException() {
        assertThrows(BERconException.class, () -> beRconClient.receiveLoginPacket());
    }

    @Test
    void shouldReceiveLoginPacketSuccess() throws IllegalAccessException {
        ByteBuffer receiveBuffer = ByteBuffer.wrap(new byte[]{0,1,2,3,4,5,6,7,0x01});
        writeField(beRconClient, "receiveBuffer", receiveBuffer, true);

        beRconClient.receiveLoginPacket();

        assertTrue(beRconClient.getConnected().get());
    }

    @Test
    void shouldReceiveLoginPacketFailure() throws IOException, IllegalAccessException {
        ByteBuffer receiveBuffer = ByteBuffer.wrap(new byte[]{0,1,2,3,4,5,6,7,0x00});
        writeField(beRconClient, "receiveBuffer", receiveBuffer, true);

        beRconClient.receiveLoginPacket();

        verify(datagramChannel).disconnect();
        verify(datagramChannelWrapper).close(any());
        verify(datagramChannelWrapper).close(any());
    }

    @Test
    void shouldReceiveLoginPacketUnknown() throws IOException, IllegalAccessException {
        ByteBuffer receiveBuffer = ByteBuffer.wrap(new byte[]{0,1,2,3,4,5,6,7,8,0x05});
        writeField(beRconClient, "receiveBuffer", receiveBuffer, true);

        beRconClient.receiveLoginPacket();

        verify(datagramChannel).disconnect();
        verify(datagramChannelWrapper).close(any());
    }

    @Test
    void shouldReceiveCommandPacketMessage() throws IOException {
        when(beCommand.getMessageType()).thenReturn(BEMessageType.COMMAND);
        when(datagramChannel.isConnected()).thenReturn(true);
        when(beCommand.getCommand()).thenReturn("command");
        when(commandQueue.poll()).thenReturn(beCommand);

        beRconClient.receiveCommandPacket();

        verify(datagramChannel).write((ByteBuffer) any());
    }

    @Test
    void shouldReceiveCommandPacket() throws IOException, IllegalAccessException {
        when(beCommand.getMessageType()).thenReturn(BEMessageType.COMMAND);
        when(datagramChannel.isConnected()).thenReturn(true);
        when(beCommand.getCommand()).thenReturn("command");
        when(commandQueue.poll()).thenReturn(beCommand);

        ByteBuffer receiveBuffer = ByteBuffer.wrap(new byte[]{0x00,0x00,9,4,5,6,7,8,9,9,9,9,9,9});
        writeField(beRconClient, "receiveBuffer", receiveBuffer, true);

        beRconClient.receiveCommandPacket();

        verify(datagramChannel).write((ByteBuffer) any());
    }

    @Test
    void shouldReceiveServerPacket() throws IOException {
        when(datagramChannel.isConnected()).thenReturn(true);
        when(commandQueue.poll()).thenReturn(beCommand);
        when(beCommand.getMessageType()).thenReturn(BEMessageType.COMMAND);
        when(beCommand.getCommand()).thenReturn("command");

        beRconClient.receiveServerPacket();

        verify(datagramChannel, times(2)).write((ByteBuffer) any());
    }

    @Test
    void shouldNotSendNextCommand() throws IOException {
        when(beCommand.getMessageType()).thenReturn(BEMessageType.COMMAND);
        when(datagramChannel.isConnected()).thenReturn(true);
        when(commandQueue.poll()).thenReturn(beCommand);
        doThrow(IOException.class).when(datagramChannel).write((ByteBuffer) any());

        assertThrows(BERconException.class, () -> beRconClient.sendNextCommand());
    }

    @Test
    void shouldSendNextCommand() throws IOException {
        when(beCommand.getMessageType()).thenReturn(BEMessageType.COMMAND);
        when(datagramChannel.isConnected()).thenReturn(true);
        when(commandQueue.poll()).thenReturn(beCommand);

        beRconClient.sendNextCommand();

        verify(datagramChannel).write((ByteBuffer) any());
    }

    @Test
    void shouldSentKeepAlive() throws IOException {
        when(datagramChannel.isConnected()).thenReturn(true);

        beRconClient.sentKeepAlive();

        verify(datagramChannel).write((ByteBuffer) any());
    }

    @Test
    void shouldNotDisconnect() throws IOException {
        doThrow(IOException.class).when(datagramChannelWrapper).close(any());

        beRconClient.disconnect();

        verify(datagramChannel).disconnect();
    }

    @Test
    void shouldDisconnect() throws IOException {
        beRconClient.disconnect();

        verify(datagramChannel).disconnect();
        verify(datagramChannelWrapper).close(any());
    }

    @Test
    void shouldCheckTimeout() throws IOException {
        doNothing().when(datagramChannelWrapper).close(datagramChannel);

        beRconClient.checkTimeout();

        verify(datagramChannelWrapper).close(any());
    }

    @Test
    void shouldConnect() throws IOException {
        when(datagramChannelWrapper.open()).thenReturn(datagramChannel);
        when(datagramChannel.connect(any())).thenReturn(datagramChannel);
        when(datagramChannel.getOption(StandardSocketOptions.SO_SNDBUF)).thenReturn(99999);
        when(datagramChannel.getOption(StandardSocketOptions.SO_RCVBUF)).thenReturn(99999);
        when(datagramChannel.isConnected()).thenReturn(true);
        when(datagramChannel.write((ByteBuffer) any())).thenReturn(1);
        when(configuration.getConnectionDelay()).thenReturn(1L);

        beRconClient.connect(beLoginCredential);

        verify(datagramChannel).write((ByteBuffer) any());
        assertNotNull(beRconClient.getMonitorThread());
        assertNotNull(beRconClient.getReceiveThread());
    }

    @Test
    void shouldNotConnect() throws IOException, IllegalAccessException {
        Thread receiveThread = mock(Thread.class);

        writeField(beRconClient, "receiveThread", receiveThread, true);

        when(datagramChannelWrapper.open()).thenReturn(datagramChannel);
        when(datagramChannel.getOption(StandardSocketOptions.SO_SNDBUF)).thenReturn(99999);
        when(datagramChannel.getOption(StandardSocketOptions.SO_RCVBUF)).thenReturn(99999);
        doThrow(RuntimeException.class).when(receiveThread).start();

        assertThrows(BERconException.class, () -> beRconClient.connect(beLoginCredential));
    }

    @Test
    void shouldSendCommandString() {
        beRconClient.sendCommand("test command");
        verify(commandQueue).add(any());
    }

    @Test
    void shouldSendCommandNoArgs() {
        beRconClient.sendCommand(DayzBECommandType.EMPTY);
        verify(commandQueue).add(any());
    }

    @Test
    void shouldSendCommandArgs() {
        beRconClient.sendCommand(DayzBECommandType.EMPTY, "arg1", "arg2");
        verify(commandQueue).add(any());
    }

    private static class CustomConnectionHandler implements ConnectionHandler {
        public void onConnected(BEConnectType connectType) {
            System.out.println("Connected!");
        }

        public void onDisconnected(BEDisconnectType disconnectType) {
            System.out.println("Disconnected!");
        }
    }

    private static class CustomResponseHandler implements ResponseHandler {
        @Override
        public void onResponse(String response) {
            System.out.println(response);
        }
    }

    private static class TestLogImpl implements LogWrapper {
        public void debug(String msg) {
            System.out.println(msg);
        }
        public void info(String msg) {
            System.out.println(msg);
        }
        public void warn(String msg) {
            System.out.println(msg);
        }
        public void warn(String msg, Throwable t) {
            System.out.println(msg);
        }
    }
}
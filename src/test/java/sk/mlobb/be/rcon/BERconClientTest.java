package sk.mlobb.be.rcon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.mlobb.be.rcon.model.BECommand;
import sk.mlobb.be.rcon.model.BELoginCredential;
import sk.mlobb.be.rcon.model.command.DayzBECommandType;
import sk.mlobb.be.rcon.model.configuration.BERconConfiguration;
import sk.mlobb.be.rcon.model.enums.BEMessageType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class BERconClientTest {

    @InjectMocks
    private BERconClient beRconClient;

    @Test
    void shouldSendNextCommand() throws IllegalAccessException, IOException {
        Queue<BECommand> commandQueue = mock(Queue.class);
        BECommand beCommand = mock(BECommand.class);
        AtomicInteger sequenceNumber = new AtomicInteger(0);
        ByteBuffer sendBuffer = ByteBuffer.allocate(9999);
        DatagramChannel datagramChannel = mock(DatagramChannel.class);
        AtomicLong lastSentTime = new AtomicLong(100L);
        BERconConfiguration configuration = mock(BERconConfiguration.class);
        configuration.setConnectionDelay(1L);

        writeField(beRconClient, "datagramChannel", datagramChannel, true);
        writeField(beRconClient, "sequenceNumber", sequenceNumber , true);
        writeField(beRconClient, "commandQueue", commandQueue, true);
        writeField(beRconClient, "sendBuffer", sendBuffer, true);
        writeField(beRconClient, "lastSentTime", lastSentTime, true);
        writeField(beRconClient, "beRconConfiguration", configuration, true);

        when(beCommand.getMessageType()).thenReturn(BEMessageType.COMMAND);
        when(datagramChannel.isConnected()).thenReturn(true);
        when(commandQueue.poll()).thenReturn(beCommand);

        beRconClient.sendNextCommand();

        verify(datagramChannel).write((ByteBuffer) any());
    }

    @Test
    void shouldSentKeepAlive() throws IllegalAccessException, IOException {
        BERconConfiguration configuration = mock(BERconConfiguration.class);
        configuration.setTimeoutTime(1L);
        AtomicLong lastSentTime = new AtomicLong(100L);
        AtomicInteger sequenceNumber = new AtomicInteger(0);
        ByteBuffer sendBuffer = ByteBuffer.allocate(9999);
        DatagramChannel datagramChannel = mock(DatagramChannel.class);

        writeField(beRconClient, "datagramChannel", datagramChannel, true);
        writeField(beRconClient, "sendBuffer", sendBuffer, true);
        writeField(beRconClient, "sequenceNumber", sequenceNumber , true);
        writeField(beRconClient, "lastSentTime", lastSentTime, true);
        writeField(beRconClient, "beRconConfiguration", configuration, true);

        when(datagramChannel.isConnected()).thenReturn(true);

        beRconClient.sentKeepAlive();

        verify(datagramChannel).write((ByteBuffer) any());
    }

    @Test
    void shouldDisconnect() throws IllegalAccessException, IOException {
        DatagramChannel datagramChannel = mock(DatagramChannel.class);
        DatagramChannelWrapper datagramChannelWrapper = mock(DatagramChannelWrapper.class);
        BERconConfiguration configuration = mock(BERconConfiguration.class);
        configuration.setTimeoutTime(1L);
        AtomicLong lastReceivedTime = new AtomicLong(1L);
        AtomicLong lastSentTime = new AtomicLong(100L);

        writeField(beRconClient, "lastReceivedTime", lastReceivedTime, true);
        writeField(beRconClient, "lastSentTime", lastSentTime, true);
        writeField(beRconClient, "datagramChannelWrapper", datagramChannelWrapper, true);
        writeField(beRconClient, "datagramChannel", datagramChannel, true);
        writeField(beRconClient, "beRconConfiguration", configuration, true);

        beRconClient.disconnect();

        verify(datagramChannelWrapper).close(any());
    }

    @Test
    void shouldCheckTimeout() throws IllegalAccessException, IOException {
        DatagramChannel datagramChannel = mock(DatagramChannel.class);
        DatagramChannelWrapper datagramChannelWrapper = mock(DatagramChannelWrapper.class);
        BERconConfiguration configuration = mock(BERconConfiguration.class);
        configuration.setTimeoutTime(1L);
        AtomicLong lastReceivedTime = new AtomicLong(1L);
        AtomicLong lastSentTime = new AtomicLong(100L);

        writeField(beRconClient, "lastReceivedTime", lastReceivedTime, true);
        writeField(beRconClient, "lastSentTime", lastSentTime, true);
        writeField(beRconClient, "datagramChannelWrapper", datagramChannelWrapper, true);
        writeField(beRconClient, "datagramChannel", datagramChannel, true);
        writeField(beRconClient, "beRconConfiguration", configuration, true);

        doNothing().when(datagramChannelWrapper).close(datagramChannel);

        beRconClient.checkTimeout();

        verify(datagramChannelWrapper).close(any());
    }

    @Test
    void shouldConnect() throws IOException, IllegalAccessException {
        BELoginCredential beLoginCredential = mock(BELoginCredential.class);
        DatagramChannel datagramChannel = mock(DatagramChannel.class);
        DatagramChannelWrapper datagramChannelWrapper = mock(DatagramChannelWrapper.class);
        BERconConfiguration configuration = mock(BERconConfiguration.class);

        writeField(beRconClient, "log", new TestLogImpl(), true);
        writeField(beRconClient, "loggingEnabled", true, true);
        writeField(beRconClient, "datagramChannelWrapper", datagramChannelWrapper, true);
        writeField(beRconClient, "datagramChannel", datagramChannel, true);
        writeField(beRconClient, "beRconConfiguration", configuration, true);

        when(datagramChannelWrapper.open()).thenReturn(datagramChannel);
        when(datagramChannel.connect(any())).thenReturn(datagramChannel);
        when(datagramChannel.getOption(StandardSocketOptions.SO_SNDBUF)).thenReturn(99999);
        when(datagramChannel.getOption(StandardSocketOptions.SO_RCVBUF)).thenReturn(99999);
        when(datagramChannel.isConnected()).thenReturn(true);
        when(datagramChannel.write((ByteBuffer) any())).thenReturn(1);
        when(configuration.getConnectionDelay()).thenReturn(1L);

        beRconClient.connect(beLoginCredential);

        verify(datagramChannel).write((ByteBuffer) any());
    }

    @Test
    void shouldSendCommandString() throws IllegalAccessException {
        Queue<BECommand> commandQueue = mock(Queue.class);
        writeField(beRconClient, "commandQueue", commandQueue, true);
        beRconClient.sendCommand("test command");
        verify(commandQueue).add(any());
    }

    @Test
    void shouldSendCommandNoArgs() throws IllegalAccessException {
        Queue<BECommand> commandQueue = mock(Queue.class);
        writeField(beRconClient, "commandQueue", commandQueue, true);
        beRconClient.sendCommand(DayzBECommandType.EMPTY);
        verify(commandQueue).add(any());
    }

    @Test
    void shouldSendCommandArgs() throws IllegalAccessException {
        Queue<BECommand> commandQueue = mock(Queue.class);
        writeField(beRconClient, "commandQueue", commandQueue, true);
        beRconClient.sendCommand(DayzBECommandType.EMPTY, "arg1", "arg2");
        verify(commandQueue).add(any());
    }

    private static class TestLogImpl implements LogWrapper {
        public void debug(String msg) { }
        public void debug(String msg, Throwable t) { }
        public void info(String msg) { }
        public void info(String msg, Throwable t) { }
        public void warn(String msg) { }
        public void warn(String msg, Throwable t) { }
    }
}
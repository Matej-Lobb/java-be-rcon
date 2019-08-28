package sk.mlobb.be.rcon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.mlobb.be.rcon.model.BECommand;
import sk.mlobb.be.rcon.model.BELoginCredential;
import sk.mlobb.be.rcon.model.command.DayzBECommandType;
import sk.mlobb.be.rcon.model.configuration.BERconConfiguration;
import sk.mlobb.be.rcon.wrapper.DatagramChannelWrapper;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Queue;

import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BERconClientTest {

    @InjectMocks
    private BERconClient beRconClient;

    @Test
    void shouldConnect() throws IOException, IllegalAccessException {
        BELoginCredential beLoginCredential = mock(BELoginCredential.class);
        DatagramChannel datagramChannel = mock(DatagramChannel.class);
        DatagramChannelWrapper datagramChannelWrapper = mock(DatagramChannelWrapper.class);
        BERconConfiguration configuration = mock(BERconConfiguration.class);

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
        when(datagramChannel.read((ByteBuffer) any())).thenReturn(7);

        beRconClient.connect(beLoginCredential);

        verify(datagramChannel).write((ByteBuffer) any());
        verify(configuration).getConnectionDelay();
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
}
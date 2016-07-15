package com.eje_c.libreastream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ReaStreamSender implements AutoCloseable {

    public static final int DEFAULT_PORT = 58710;
    public static final String DEFAULT_IDENTIFIER = "default";
    private final DatagramSocket socket;
    private final DatagramPacket packet;
    private ByteBuffer buffer;
    private ReaStreamPacket reaStreamPacket = new ReaStreamPacket();

    public ReaStreamSender() throws SocketException {
        this(new DatagramSocket());
    }

    /**
     * @param socket Pre-created socket
     */
    public ReaStreamSender(DatagramSocket socket) {

        this.socket = socket;
        packet = new DatagramPacket(new byte[0], 0);

        setPort(DEFAULT_PORT);
        setIdentifier(DEFAULT_IDENTIFIER);
    }

    /**
     * Send ReaStream audio packet.
     *
     * @param audioData Audio data
     * @param readCount Must be less than or equal to audioData.length
     * @throws IOException
     */
    public void send(float[] audioData, int readCount) throws IOException {

        int offset = 0;

        // Max audio frames are ReaStreamPacket.MAX_BLOCK_LENGTH bytes.
        // Split to multiple packets.
        while (true) {

            int remaining = readCount - offset;
            if (remaining <= 0) break;

            int length = Math.min(remaining, ReaStreamPacket.MAX_BLOCK_LENGTH / ReaStreamPacket.PER_SAMPLE_BYTES);
            float[] part = Arrays.copyOfRange(audioData, offset, offset + length);

            reaStreamPacket.setAudioData(part, length);
            prepareAndSendPacket();

            offset += length;
        }
    }

    /**
     * Send ReaStream midi packet.
     *
     * @param midiEvents Midi data
     * @throws IOException
     */
    public void send(MidiEvent... midiEvents) throws IOException {
        reaStreamPacket.setMidiData(midiEvents);
        prepareAndSendPacket();
    }

    private void prepareAndSendPacket() throws IOException {

        // Create buffer
        if (!reaStreamPacket.isCapableBuffer(buffer)) {
            buffer = reaStreamPacket.createCapableBuffer();
            packet.setData(buffer.array());
        }

        reaStreamPacket.writeToBuffer(buffer);

        packet.setLength(reaStreamPacket.packetSize);
        socket.send(packet);
    }

    public void setIdentifier(String identifier) {
        reaStreamPacket.setIdentifier(identifier);
    }

    public String getIdentifier() {
        return reaStreamPacket.getIdentifier();
    }

    public void setSampleRate(int sampleRate) {
        reaStreamPacket.sampleRate = sampleRate;
    }

    public int getSampleRate() {
        return reaStreamPacket.sampleRate;
    }

    public void setChannels(byte channels) {
        reaStreamPacket.channels = channels;
    }

    public byte getChannels() {
        return reaStreamPacket.channels;
    }

    public void setRemoteAddress(InetAddress remoteAddress) {
        packet.setAddress(remoteAddress);
    }

    public InetAddress getRemoteAddress() {
        return packet.getAddress();
    }

    public void setPort(int port) {
        packet.setPort(port);
    }

    public int getPort() {
        return packet.getPort();
    }

    /**
     * Close UDP socket.
     */
    @Override
    public void close() {
        socket.close();
    }
}

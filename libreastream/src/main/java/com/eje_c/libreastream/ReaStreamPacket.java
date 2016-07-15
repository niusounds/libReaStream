package com.eje_c.libreastream;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents ReaStream packet data.
 */
public class ReaStreamPacket {

    public static final short MAX_BLOCK_LENGTH = 1200;
    public static final int PER_SAMPLE_BYTES = Float.SIZE / Byte.SIZE;
    public static final int AUDIO_PACKET_HEADER_BYTE_SIZE = 4 + 4 + 32 + 1 + 4 + 2;
    public static final int MIDI_PACKET_HEADER_BYTE_SIZE = 4 + 4 + 32;

    public int packetSize;

    /*
     * TODO It should be separated classes
     */

    // audio data
    public final byte[] identifier = new byte[32];
    public byte channels = 1;
    public int sampleRate = 44100;
    public short blockLength;
    public float[] audioData;

    // MIDI data
    public MidiEvent[] midiEvents;

    /**
     * Set identifier with String.
     *
     * @param identifier Must be less than or equal to 32 bytes.
     */
    public void setIdentifier(String identifier) {
        byte[] bytes = identifier.getBytes();
        System.arraycopy(bytes, 0, this.identifier, 0, bytes.length);
    }

    /**
     * Get identifier as String.
     *
     * @return identifier
     */
    public String getIdentifier() {
        return new String(identifier).trim();
    }

    /**
     * Read data from {@link ByteBuffer} which is read from UDP socket.
     *
     * @param buffer Buffer from received UDP raw packet.
     * @return {@code true} if buffer is ReaStream packet and have read, otherwise {@code false}.
     */
    public boolean readFromBuffer(ByteBuffer buffer) {
        buffer.position(0);

        // Audio packet
        if (buffer.get() == (byte) 77// M
                && buffer.get() == (byte) 82 // R
                && buffer.get() == (byte) 83 // S
                && buffer.get() == (byte) 82 // R
                ) {

            packetSize = buffer.getInt();
            buffer.get(identifier);
            channels = buffer.get();
            sampleRate = buffer.getInt();
            blockLength = buffer.getShort();

            final int sizeInFloats = blockLength / PER_SAMPLE_BYTES;
            if (audioData == null || audioData.length < sizeInFloats) {
                audioData = new float[sizeInFloats];
            }

            for (int i = 0; i < sizeInFloats; i++) {
                audioData[i] = buffer.getFloat();
            }

            midiEvents = null;
            return true;
        }

        buffer.position(0);

        // MIDI packet
        if (buffer.get() == (byte) 109 // m
                && buffer.get() == (byte) 82  // R
                && buffer.get() == (byte) 83  // S
                && buffer.get() == (byte) 82  // R
                ) {

            packetSize = buffer.getInt();
            buffer.get(identifier);

            int eventsBytes = packetSize - 4 - 4 - 32;
            int eventCount = eventsBytes / MidiEvent.BYTE_SIZE;

            midiEvents = new MidiEvent[eventCount];
            for (int i = 0; i < eventCount; i++) {
                final MidiEvent midiEvent = new MidiEvent();
                midiEvent.type = buffer.getInt();
                midiEvent.byteSize = buffer.getInt();
                midiEvent.sampleFramesSinceLastEvet = buffer.getInt();
                midiEvent.flags = buffer.getInt();
                midiEvent.noteLength = buffer.getInt();
                midiEvent.noteOffset = buffer.getInt();
                buffer.get(midiEvent.midiData);
                buffer.get(); // Reserved 0
                midiEvent.detune = buffer.get();
                midiEvent.noteOffVelocity = buffer.get();
                midiEvents[i] = midiEvent;
                buffer.get(); // Reserved 0
                buffer.get(); // Reserved 0
            }

            audioData = null;
            return true;
        }

        return false;
    }

    /**
     * Write data to {@link ByteBuffer} which will be sent to UDP socket.
     *
     * @param buffer
     */
    public void writeToBuffer(ByteBuffer buffer) {

        buffer.position(0);

        if (isAudioData()) {

            buffer.put((byte) 77); // M
            buffer.put((byte) 82); // R
            buffer.put((byte) 83); // S
            buffer.put((byte) 82); // R
            buffer.putInt(packetSize);
            buffer.put(identifier);
            buffer.put(channels); // ch
            buffer.putInt(sampleRate);
            buffer.putShort(blockLength);

            for (int i = 0; i < blockLength / PER_SAMPLE_BYTES; i++) {
                buffer.putFloat(audioData[i]);
            }

        } else if (isMidiData()) {

            buffer.put((byte) 109); // M
            buffer.put((byte) 82); // R
            buffer.put((byte) 83); // S
            buffer.put((byte) 82); // R
            buffer.putInt(packetSize);
            buffer.put(identifier);

            for (MidiEvent midiEvent : midiEvents) {

                buffer.putInt(midiEvent.type);
                buffer.putInt(midiEvent.byteSize);
                buffer.putInt(midiEvent.sampleFramesSinceLastEvet);
                buffer.putInt(midiEvent.flags);
                buffer.putInt(midiEvent.noteLength);
                buffer.putInt(midiEvent.noteOffset);
                buffer.put(midiEvent.midiData);
                buffer.put((byte) 0); // Reserved 0
                buffer.put(midiEvent.detune);
                buffer.put(midiEvent.noteOffVelocity);
                buffer.put((byte) 0); // Reserved 0
                buffer.put((byte) 0); // Reserved 0
            }
        }
    }

    /**
     * Set audio data.
     * Usually, audio data is read from {@link android.media.AudioRecord#read(float[], int, int, int)}.
     * Currently supported mono audioData only.
     *
     * @param audioData   Audio data.
     * @param sampleCount Valid audio data sample count. Must be less than or equal to {@code audioData.length}.
     */
    public void setAudioData(float[] audioData, int sampleCount) {

        // TODO 2 or above channels support
        this.blockLength = (short) (sampleCount * PER_SAMPLE_BYTES);
        this.packetSize = AUDIO_PACKET_HEADER_BYTE_SIZE + blockLength;
        this.audioData = audioData;
        this.midiEvents = null;
    }

    /**
     * Set MIDI data.
     *
     * @param midiEvents MIDI data.
     */
    public void setMidiData(MidiEvent... midiEvents) {

        this.packetSize = MIDI_PACKET_HEADER_BYTE_SIZE + MidiEvent.BYTE_SIZE * midiEvents.length;
        this.midiEvents = midiEvents;
        this.audioData = null;
    }

    /**
     * @param buffer
     * @return {@code true} if buffer can be passed to {@link #writeToBuffer(ByteBuffer)}, otherwise {@code false}.
     */
    public boolean isCapableBuffer(ByteBuffer buffer) {
        return buffer != null && buffer.capacity() >= packetSize;
    }

    /**
     * Create buffer which can be passed to {@link #writeToBuffer(ByteBuffer)}.
     *
     * @return created buffer.
     */
    public ByteBuffer createCapableBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(packetSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    /**
     * {@link #audioData} is not interleaved.
     * This method puts interleaved audio data to {@code outInterleavedAudioData}.
     *
     * @param outInterleavedAudioData Output audio data.
     */
    public void getInterleavedAudioData(float[] outInterleavedAudioData) {

        int samples = blockLength / ReaStreamPacket.PER_SAMPLE_BYTES;
        int samplesPerChannel = samples / channels;

        for (int i = 0; i < samplesPerChannel; i++) {
            for (int ch = 0; ch < channels; ch++) {
                outInterleavedAudioData[i * channels + ch] = audioData[samplesPerChannel * ch + i];
            }
        }
    }

    public boolean isAudioData() {
        return audioData != null;
    }

    public boolean isMidiData() {
        return midiEvents != null;
    }
}

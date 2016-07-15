package com.eje_c.libreastream;

import java.util.Arrays;

public class MidiEvent {
    public static final int BYTE_SIZE = 4 + 4 + 4 + 4 + 4 + 4 + 3 + 1 + 1 + 1 + 2;
    public static final int NOTE_OFF = 0x80;
    public static final int NOTE_ON = 0x90;
    public static final int POLY_PRESSURE = 0xA0;
    public static final int CONTROL_CHANGE = 0xB0;
    public static final int PROGRAM_CHANGE = 0xC0;
    public static final int CHANNEL_PRESSURE = 0xD0;
    public static final int PITCH_BEND = 0xE0;

    public int type = 1;
    public int byteSize = 24;
    public int sampleFramesSinceLastEvet;
    public int flags;
    public int noteLength;
    public int noteOffset;
    public final byte[] midiData = new byte[3];
    public byte detune;
    public byte noteOffVelocity;

    @Override
    public String toString() {
        return "MidiEvent{" +
                "type=" + type +
                ", byteSize=" + byteSize +
                ", sampleFramesSinceLastEvet=" + sampleFramesSinceLastEvet +
                ", flags=" + flags +
                ", noteLength=" + noteLength +
                ", noteOffset=" + noteOffset +
                ", midiData=" + Arrays.toString(midiData) +
                ", detune=" + detune +
                ", noteOffVelocity=" + noteOffVelocity +
                '}';
    }

    public static MidiEvent create(int command, int channel, int data1, int data2) {
        MidiEvent midiEvent = new MidiEvent();
        midiEvent.midiData[0] = (byte) (command | channel);
        midiEvent.midiData[1] = (byte) data1;
        midiEvent.midiData[2] = (byte) data2;
        return midiEvent;
    }
}

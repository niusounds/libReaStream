package com.eje_c.libreastream

import java.util.*

class MidiEvent {

    @JvmField
    var type = 1
    @JvmField
    var byteSize = 24
    @JvmField
    var sampleFramesSinceLastEvet: Int = 0
    @JvmField
    var flags: Int = 0
    @JvmField
    var noteLength: Int = 0
    @JvmField
    var noteOffset: Int = 0
    @JvmField
    val midiData = ByteArray(3)
    @JvmField
    var detune: Byte = 0
    @JvmField
    var noteOffVelocity: Byte = 0

    override fun toString(): String {
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
                '}'
    }

    companion object {
        const val BYTE_SIZE = 4 + 4 + 4 + 4 + 4 + 4 + 3 + 1 + 1 + 1 + 2
        const val NOTE_OFF = 0x80
        const val NOTE_ON = 0x90
        const val POLY_PRESSURE = 0xA0
        const val CONTROL_CHANGE = 0xB0
        const val PROGRAM_CHANGE = 0xC0
        const val CHANNEL_PRESSURE = 0xD0
        const val PITCH_BEND = 0xE0

        @JvmStatic
        fun create(command: Int, channel: Int, data1: Int, data2: Int): MidiEvent {
            val midiEvent = MidiEvent()
            midiEvent.midiData[0] = (command or channel).toByte()
            midiEvent.midiData[1] = data1.toByte()
            midiEvent.midiData[2] = data2.toByte()
            return midiEvent
        }
    }
}

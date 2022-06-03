package com.niusounds.libreastream

enum class MidiCommand(val value: Int) {
    NoteOff(0x80),
    NoteOn(0x90),
    PolyPressure(0xA0),
    ControlChange(0xB0),
    ProgramChange(0xC0),
    ChannelPressure(0xD0),
    PitchBend(0xE0),
}

/**
 * Build MIDI message bytes from components.
 * [channel] must be in 0 - 15
 * [data1] and [data2] must be in 0 - 127.
 */
fun midiData(command: Int, channel: Int, data1: Int, data2: Int): ByteArray {
    return byteArrayOf(
        (command or channel).toByte(),
        data1.toByte(),
        data2.toByte(),
    )
}

/**
 * Build MIDI message bytes from components.
 * [channel] must be in 0 - 15
 * [data1] and [data2] must be in 0 - 127.
 */
fun midiData(command: MidiCommand, channel: Int, data1: Int, data2: Int): ByteArray {
    return byteArrayOf(
        (command.value or channel).toByte(),
        data1.toByte(),
        data2.toByte(),
    )
}

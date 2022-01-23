package com.niusounds.libreastream.sender

import com.niusounds.libreastream.receiver.MidiEvent
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MidiDataSerializer(
    private val identifier: String,
) {
    private val magicNumberBytes = byteArrayOf(
        'm'.code.toByte(),
        'R'.code.toByte(),
        'S'.code.toByte(),
        'R'.code.toByte(),
    )
    private val identifierBytes = ByteArray(32)

    init {
        check(identifier.length < 32) { "identifier's length must be less than 32" }

        // fill identifierBytes
        identifier.withIndex().forEach { (index, char) ->
            identifierBytes[index] = char.code.toByte()
        }
    }

    /**
     * Serializes a MIDI message into [ByteBuffer] which will be parsed into midi event
     * by remote side ReaStream VST plugin (or libReaStream).
     */
    fun toByteBuffer(
        sampleFramesSinceLastEvent: Int = 0,
        flags: Int = 0,
        noteLength: Int = 0,
        noteOffset: Int = 0,
        midiData: ByteArray,
        detune: Byte = 0,
        noteOffVelocity: Byte = 0,
    ): ByteBuffer {

        val packetSize = 4 + 4 + 32 + MidiEvent.BYTE_SIZE

        val type = 1
        val byteSize = 24

        // Create packet raw bytes for testing
        return ByteBuffer.allocate(packetSize).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(magicNumberBytes)
            putInt(packetSize)
            put(identifierBytes)

            putInt(type)
            putInt(byteSize)
            putInt(sampleFramesSinceLastEvent)
            putInt(flags)
            putInt(noteLength)
            putInt(noteOffset)
            put(midiData)
            put(0) // zero reserved
            put(detune)
            put(noteOffVelocity)
            put(0) // zero reserved
            put(0) // zero reserved

            flip()
        }
    }
}
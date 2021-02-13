package com.niusounds.libreastream.flow

import java.nio.ByteBuffer
import kotlin.math.min

/**
 * Represents ReaStream packet data.
 */
internal class ByteBufferReaStreamPacket(private val buffer: ByteBuffer) : ReaStreamPacket {

    companion object {
        private val audioIdentifier = byteArrayOf(77, 82, 83, 82)
        private val midiIdentifier = byteArrayOf(109, 82, 83, 82)
        private const val packetSizeOffset = 4
        private const val identifierOffset = 8

        // audio
        private const val channelOffset = 40
        private const val sampleRateOffset = 41
        private const val blockLengthOffset = 45
        private const val audioDataOffset = 47

        // MIDI
        private const val midiEventsOffset = 40

        const val AUDIO_PACKET_HEADER_BYTE_SIZE = 4 + 4 + 32 + 1 + 4 + 2
        const val MIDI_PACKET_HEADER_BYTE_SIZE = 4 + 4 + 32
    }

    override val isAudio: Boolean
        get() = buffer[0] == audioIdentifier[0]
                && buffer[1] == audioIdentifier[1]
                && buffer[2] == audioIdentifier[2]
                && buffer[3] == audioIdentifier[3]

    override val isMidi: Boolean
        get() = buffer[0] == midiIdentifier[0]
                && buffer[1] == midiIdentifier[1]
                && buffer[2] == midiIdentifier[2]
                && buffer[3] == midiIdentifier[3]

    override val packetSize: Int get() = buffer.getInt(packetSizeOffset)

    override val identifier: String
        get() {
            val originalPos = buffer.position()
            val array = ByteArray(32)
            buffer.position(identifierOffset)
            buffer.get(array)
            buffer.position(originalPos)
            return String(array).trim { it <= ' ' }
        }

    override val channels: Byte
        get() = buffer.get(channelOffset)

    override val sampleRate: Int
        get() = buffer.getInt(sampleRateOffset)

    override val blockLength: Short
        get() = buffer.getShort(blockLengthOffset)

    override fun readAudio(out: FloatArray, offset: Int, size: Int): Int {
        val originalPos = buffer.position()
        buffer.position(audioDataOffset)
        val sizeInFloats = min(blockLength.toInt() / ReaStreamPacket.PER_SAMPLE_BYTES, size)

        for (i in 0 until sizeInFloats) {
            out[i + offset] = buffer.float
        }
        buffer.position(originalPos)
        return sizeInFloats
    }

    override val midiEvents: List<MidiEvent>
        get() {
            val originalPos = buffer.position()

            val eventsBytes = packetSize - 4 - 4 - 32
            val eventCount = eventsBytes / MidiEvent.BYTE_SIZE

            val midiEvents = mutableListOf<MidiEvent>()
            buffer.position(midiEventsOffset)

            for (i in 0 until eventCount) {
                val midiData = ByteArray(3)
                val midiEvent = MidiEvent(
                    type = buffer.int,
                    byteSize = buffer.int,
                    sampleFramesSinceLastEvent = buffer.int,
                    flags = buffer.int,
                    noteLength = buffer.int,
                    noteOffset = buffer.int,
                    midiData = midiData.also { buffer.get(it) },
                    reserved = buffer.get(),
                    detune = buffer.get(),
                    noteOffVelocity = buffer.get(),
                )

                midiEvents += midiEvent
                buffer.get() // Reserved 0
                buffer.get() // Reserved 0
            }

            buffer.position(originalPos)
            return midiEvents
        }
}

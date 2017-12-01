package com.eje_c.libreastream

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Represents ReaStream packet data.
 */
class ReaStreamPacket {

    /**
     * Total packet size.
     */
    @JvmField
    var packetSize: Int = 0

    /**
     * Identifier raw bytes.
     */
    @JvmField
    val identifier = ByteArray(32)

    // audio data
    /**
     * Audio channels.
     */
    @JvmField
    var channels: Byte = 1

    /**
     * Audio sample rate.
     */
    @JvmField
    var sampleRate = 44100

    /**
     * Audio data block length.
     */
    @JvmField
    var blockLength: Short = 0

    /**
     * Audio data.
     */
    @JvmField
    var audioData: FloatArray? = null

    // MIDI data
    @JvmField
    var midiEvents: Array<MidiEvent>? = null

    /**
     * If true, [channels],[sampleRate],[blockLength],[audioData] can be accessible.
     */
    val isAudioData: Boolean
        get() = audioData != null

    /**
     * If true, [midiEvents] can be accessible.
     */
    val isMidiData: Boolean
        get() = midiEvents != null

    /**
     * Set identifier with String.
     *
     * @param identifier Must be less than or equal to 32 bytes.
     */
    fun setIdentifier(identifier: String) {
        val bytes = identifier.toByteArray()
        System.arraycopy(bytes, 0, this.identifier, 0, bytes.size)
    }

    /**
     * Get identifier as String.
     *
     * @return identifier
     */
    fun getIdentifier(): String {
        return String(identifier).trim { it <= ' ' }
    }

    /**
     * Read data from [ByteBuffer] which is read from UDP socket.
     *
     * @param buffer Buffer from received UDP raw packet.
     * @return `true` if buffer is ReaStream packet and have read, otherwise `false`.
     */
    fun readFromBuffer(buffer: ByteBuffer): Boolean {
        buffer.position(0)

        // Audio packet
        if (buffer.get() == 77.toByte()// M
                && buffer.get() == 82.toByte() // R
                && buffer.get() == 83.toByte() // S
                && buffer.get() == 82.toByte() // R
                ) {

            packetSize = buffer.int
            buffer.get(identifier)
            channels = buffer.get()
            sampleRate = buffer.int
            blockLength = buffer.short

            val sizeInFloats = blockLength / PER_SAMPLE_BYTES
            if (audioData == null || audioData!!.size < sizeInFloats) {
                audioData = FloatArray(sizeInFloats)
            }

            for (i in 0 until sizeInFloats) {
                audioData!![i] = buffer.float
            }

            midiEvents = null
            return true
        }

        buffer.position(0)

        // MIDI packet
        if (buffer.get() == 109.toByte() // m
                && buffer.get() == 82.toByte()  // R
                && buffer.get() == 83.toByte()  // S
                && buffer.get() == 82.toByte()  // R
                ) {

            packetSize = buffer.int
            buffer.get(identifier)

            val eventsBytes = packetSize - 4 - 4 - 32
            val eventCount = eventsBytes / MidiEvent.BYTE_SIZE

            val midiEvents = mutableListOf<MidiEvent>()
            for (i in 0 until eventCount) {

                val midiEvent = MidiEvent().apply {
                    type = buffer.int
                    byteSize = buffer.int
                    sampleFramesSinceLastEvet = buffer.int
                    flags = buffer.int
                    noteLength = buffer.int
                    noteOffset = buffer.int
                    buffer.get(midiData)
                    buffer.get() // Reserved 0
                    detune = buffer.get()
                    noteOffVelocity = buffer.get()
                }

                midiEvents += midiEvent

                buffer.get() // Reserved 0
                buffer.get() // Reserved 0
            }
            this.midiEvents = midiEvents.toTypedArray()

            audioData = null
            return true
        }

        return false
    }

    /**
     * Write data to [ByteBuffer] which will be sent to UDP socket.
     *
     * @param buffer
     */
    fun writeToBuffer(buffer: ByteBuffer) {

        buffer.position(0)

        if (isAudioData) {

            buffer.put(77.toByte()) // M
            buffer.put(82.toByte()) // R
            buffer.put(83.toByte()) // S
            buffer.put(82.toByte()) // R
            buffer.putInt(packetSize)
            buffer.put(identifier)
            buffer.put(channels) // ch
            buffer.putInt(sampleRate)
            buffer.putShort(blockLength)

            for (i in 0 until blockLength / PER_SAMPLE_BYTES) {
                buffer.putFloat(audioData!![i])
            }

        } else if (isMidiData) {

            buffer.put(109.toByte()) // M
            buffer.put(82.toByte()) // R
            buffer.put(83.toByte()) // S
            buffer.put(82.toByte()) // R
            buffer.putInt(packetSize)
            buffer.put(identifier)

            for (midiEvent in midiEvents!!) {

                buffer.putInt(midiEvent.type)
                buffer.putInt(midiEvent.byteSize)
                buffer.putInt(midiEvent.sampleFramesSinceLastEvet)
                buffer.putInt(midiEvent.flags)
                buffer.putInt(midiEvent.noteLength)
                buffer.putInt(midiEvent.noteOffset)
                buffer.put(midiEvent.midiData)
                buffer.put(0.toByte()) // Reserved 0
                buffer.put(midiEvent.detune)
                buffer.put(midiEvent.noteOffVelocity)
                buffer.put(0.toByte()) // Reserved 0
                buffer.put(0.toByte()) // Reserved 0
            }
        }
    }

    /**
     * Set audio data.
     * Usually, audio data is read from [android.media.AudioRecord.read].
     * Currently supported mono audioData only.
     *
     * @param audioData   Audio data.
     * @param sampleCount Valid audio data sample count. Must be less than or equal to `audioData.length`.
     */
    fun setAudioData(audioData: FloatArray, sampleCount: Int) {

        // TODO 2 or above channels support
        this.blockLength = (sampleCount * PER_SAMPLE_BYTES).toShort()
        this.packetSize = AUDIO_PACKET_HEADER_BYTE_SIZE + blockLength
        this.audioData = audioData
        this.midiEvents = null
    }

    /**
     * Set MIDI data.
     *
     * @param midiEvents MIDI data.
     */
    fun setMidiData(vararg midiEvents: MidiEvent) {

        this.packetSize = MIDI_PACKET_HEADER_BYTE_SIZE + MidiEvent.BYTE_SIZE * midiEvents.size
        this.midiEvents = midiEvents.toList().toTypedArray()
        this.audioData = null
    }

    /**
     * @param buffer
     * @return `true` if buffer can be passed to [writeToBuffer], otherwise `false`.
     */
    fun isCapableBuffer(buffer: ByteBuffer?): Boolean {
        return buffer != null && buffer.capacity() >= packetSize
    }

    /**
     * Create buffer which can be passed to [writeToBuffer].
     *
     * @return created buffer.
     */
    fun createCapableBuffer(): ByteBuffer {
        val buffer = ByteBuffer.allocate(packetSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        return buffer
    }

    companion object {
        const val MAX_BLOCK_LENGTH: Short = 1200
        const val PER_SAMPLE_BYTES = java.lang.Float.SIZE / java.lang.Byte.SIZE
        const val AUDIO_PACKET_HEADER_BYTE_SIZE = 4 + 4 + 32 + 1 + 4 + 2
        const val MIDI_PACKET_HEADER_BYTE_SIZE = 4 + 4 + 32
    }
}

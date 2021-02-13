package com.niusounds.libreastream.flow

/**
 * Represents ReaStream packet data.
 */
interface ReaStreamPacket {
    val isAudio: Boolean
    val isMidi: Boolean
    val packetSize: Int
    val identifier: String
    val channels: Byte
    val sampleRate: Int
    val blockLength: Short

    /**
     * Get PCM audio data. Must be called only when [isAudio] is true.
     */
    fun readAudio(out: FloatArray, offset: Int = 0, size: Int = out.size): Int

    /**
     * Get MIDI event data. Must be called only when [isMidi] is true.
     */
    val midiEvents: List<MidiEvent>

    companion object {
        const val MAX_BLOCK_LENGTH = 1200
        const val PER_SAMPLE_BYTES = Float.SIZE_BYTES
    }
}

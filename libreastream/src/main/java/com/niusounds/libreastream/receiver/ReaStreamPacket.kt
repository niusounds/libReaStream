package com.niusounds.libreastream.receiver

/**
 * Represents ReaStream packet data.
 */
interface ReaStreamPacket {
    /**
     * Returns true if this packet has audio data.
     */
    val isAudio: Boolean

    /**
     * Returns true if this packet has MIDI data.
     */
    val isMidi: Boolean

    /**
     * Packet size in bytes.
     */
    val packetSize: Int

    /**
     * Identifier
     */
    val identifier: String

    /**
     * Use this only if [isAudio] is true.
     * Otherwise this value is undefined.
     */
    val channels: Byte

    /**
     * Use this only if [isAudio] is true.
     * Otherwise this value is undefined.
     */
    val sampleRate: Int

    /**
     * Use this only if [isAudio] is true.
     * Otherwise this value is undefined.
     */
    val blockLength: Short

    /**
     * Get PCM audio data.
     * Must be used this only if [isAudio] is true.
     * Otherwise reads undefined values.
     */
    fun readAudio(out: FloatArray, offset: Int = 0, size: Int = out.size): Int

    /**
     * Get PCM audio data.
     * Must be used this only if [isAudio] is true.
     * Otherwise reads undefined values.
     */
    fun readAudioInterleaved(out: FloatArray, offset: Int = 0, size: Int = out.size): Int

    /**
     * Get MIDI event data.
     * Use this only if [isMidi] is true.
     * Otherwise returns undefined values.
     */
    val midiEvents: List<MidiEvent>

    companion object {
        const val MAX_BLOCK_LENGTH = 1200
        const val PER_SAMPLE_BYTES = Float.SIZE_BYTES
    }
}

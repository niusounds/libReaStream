package com.eje_c.libreastream

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.*

class ReaStreamSender(private val channel: DatagramChannel = DatagramChannel.open()) : AutoCloseable {

    private var buffer: ByteBuffer? = null
    private val reaStreamPacket = ReaStreamPacket()

    /**
     * Identifier for ReaStream packet. Audio/MIDI data is ignored if both identifier is not same.
     */
    var identifier: String
        get() = reaStreamPacket.getIdentifier()
        set(value) = reaStreamPacket.setIdentifier(value)

    /**
     * Sample rate of audio data.
     */
    var sampleRate: Int
        get() = reaStreamPacket.sampleRate
        set(value) {
            reaStreamPacket.sampleRate = value
        }

    /**
     * Channels of audio data.
     */
    var channels: Byte
        get() = reaStreamPacket.channels
        set(value) {
            reaStreamPacket.channels = value
        }

    /**
     * Remote address. Default value is local broadcast address.
     */
    var remote: InetSocketAddress = InetSocketAddress(getBroadcastAddress().firstOrNull(), DEFAULT_PORT)

    /**
     * For compatibility from previous version. Use [remote].
     */
    var remoteAddress: InetAddress
        get() = remote.address
        set(value) {
            remote = InetSocketAddress(value, remote.port)
        }

    /**
     * For compatibility from previous version. Use [remote].
     */
    var port: Int
        get() = remote.port
        set(value) {
            remote = InetSocketAddress(remote.address, port)
        }

    init {
        identifier = DEFAULT_IDENTIFIER
    }

    /**
     * Send ReaStream audio packet.
     *
     * @param audioData Audio data
     * @param readCount Must be less than or equal to audioData.length
     * @throws IOException
     */
    @Throws(IOException::class)
    fun send(audioData: FloatArray, readCount: Int) {

        var offset = 0

        // Max audio frames are ReaStreamPacket.MAX_BLOCK_LENGTH bytes.
        // Split to multiple packets.
        while (true) {

            val remaining = readCount - offset
            if (remaining <= 0) break

            val length = Math.min(remaining, ReaStreamPacket.MAX_BLOCK_LENGTH / ReaStreamPacket.PER_SAMPLE_BYTES)
            val part = Arrays.copyOfRange(audioData, offset, offset + length)

            reaStreamPacket.setAudioData(part, length)
            prepareAndSendPacket()

            offset += length
        }
    }

    /**
     * Send ReaStream midi packet.
     *
     * @param midiEvents Midi data
     * @throws IOException
     */
    @Throws(IOException::class)
    fun send(vararg midiEvents: MidiEvent) {
        reaStreamPacket.setMidiData(*midiEvents)
        prepareAndSendPacket()
    }

    @Throws(IOException::class)
    private fun prepareAndSendPacket() {

        // Create buffer
        if (!reaStreamPacket.isCapableBuffer(buffer)) {
            buffer = reaStreamPacket.createCapableBuffer()
        }

        buffer!!.clear()
        reaStreamPacket.writeToBuffer(buffer!!)
        buffer!!.flip()

        channel.send(buffer, remote)
    }

    /**
     * Close UDP channel.
     */
    override fun close() {
        channel.close()
    }

    companion object {
        const val DEFAULT_PORT = 58710
        const val DEFAULT_IDENTIFIER = "default"
    }
}

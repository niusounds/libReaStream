package com.niusounds.libreastream

import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

/**
 * Low-level [ReaStreamPacket] sender.
 * @param remote Remote address for sending data. Default value is broadcast address for current network with [ReaStream.DEFAULT_PORT].
 * @param channel Pre-created [DatagramChannel] for reuse channel. If [close] is called, this channel is also closed.
 * @param identifier Identifier string. Max 32 bytes. Default value is "default".
 */
class ReaStreamSender(
        val remote: SocketAddress = InetSocketAddress(getBroadcastAddress().firstOrNull(), ReaStream.DEFAULT_PORT),
        val identifier: String = ReaStream.DEFAULT_IDENTIFIER) : AutoCloseable {

    private val channel: DatagramChannel = DatagramChannel.open()
    private var buffer: ByteBuffer? = null
    private val reaStreamPacket = ReaStreamPacket()

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

    init {
        reaStreamPacket.setIdentifier(identifier)

        // Use async mode
        channel.configureBlocking(false)

        // Enable broadcast packet
        channel.socket().broadcast = true
    }

    /**
     * Send ReaStream audio packet.
     *
     * @param audioData Audio data
     * @param readCount Must be less than or equal to audioData.length
     */
    fun send(audioData: FloatArray, readCount: Int) {

        var offset = 0

        // Max audio frames are ReaStreamPacket.MAX_BLOCK_LENGTH bytes.
        // Split to multiple packets.
        while (true) {

            val remaining = readCount - offset
            if (remaining <= 0) break

            val length = Math.min(remaining, ReaStreamPacket.MAX_BLOCK_LENGTH / ReaStreamPacket.PER_SAMPLE_BYTES)
            val part = audioData.copyOfRange(offset, offset + length)

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
    fun send(vararg midiEvents: MidiEvent) {
        reaStreamPacket.setMidiData(*midiEvents)
        prepareAndSendPacket()
    }

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
}

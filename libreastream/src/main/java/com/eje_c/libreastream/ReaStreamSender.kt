package com.eje_c.libreastream

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.*

class ReaStreamSender
/**
 * @param socket Pre-created socket
 */
@JvmOverloads constructor(private val socket: DatagramSocket = DatagramSocket()) : AutoCloseable {
    private val packet: DatagramPacket = DatagramPacket(ByteArray(0), 0)
    private var buffer: ByteBuffer? = null
    private val reaStreamPacket = ReaStreamPacket()

    var identifier: String
        get() = reaStreamPacket.getIdentifier()
        set(identifier) = reaStreamPacket.setIdentifier(identifier)

    var sampleRate: Int
        get() = reaStreamPacket.sampleRate
        set(sampleRate) {
            reaStreamPacket.sampleRate = sampleRate
        }

    var channels: Byte
        get() = reaStreamPacket.channels
        set(channels) {
            reaStreamPacket.channels = channels
        }

    var remoteAddress: InetAddress
        get() = packet.address
        set(remoteAddress) {
            packet.address = remoteAddress
        }

    var port: Int
        get() = packet.port
        set(port) {
            packet.port = port
        }

    init {
        port = DEFAULT_PORT
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
            packet.data = buffer!!.array()
        }

        reaStreamPacket.writeToBuffer(buffer!!)

        packet.length = reaStreamPacket.packetSize
        socket.send(packet)
    }

    /**
     * Close UDP socket.
     */
    override fun close() {
        socket.close()
    }

    companion object {
        const val DEFAULT_PORT = 58710
        const val DEFAULT_IDENTIFIER = "default"
    }
}

package com.niusounds.libreastream

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import kotlin.concurrent.thread

/**
 * Low-level [ReaStreamPacket] receiver.
 *
 * @param channel Pre-created [DatagramChannel] for reuse channel. If [close] is called, this channel is also closed.
 * @param identifier Identifier string. Max 32 bytes. Default value is "default".
 * @param port Waiting port number. Default value is [ReaStream.DEFAULT_PORT] which is used by original ReaStream plugin.
 */
class ReaStreamReceiver(
        val identifier: String = ReaStream.DEFAULT_IDENTIFIER,
        val port: Int = ReaStream.DEFAULT_PORT,
        var audioPacketHandler: AudioPacketHandler? = null,
        val midiPacketHandler: MidiPacketHandler? = null) : AutoCloseable {

    private val channel: DatagramChannel = DatagramChannel.open()
    private val reaStreamPacket = ReaStreamPacket()
    private val buffer: ByteBuffer = ByteBuffer.allocate(ReaStreamPacket.MAX_BLOCK_LENGTH + ReaStreamPacket.AUDIO_PACKET_HEADER_BYTE_SIZE)
            .apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }
    private var closed: Boolean = false


    init {
        channel.socket().bind(InetSocketAddress(port))
        channel.configureBlocking(false)

        // Start async receiving thread
        thread {
            val selector = Selector.open()
            channel.register(selector, SelectionKey.OP_READ)

            // Stop when close() is called
            while (!closed) {

                selector.select()
                val iterator = selector.selectedKeys().iterator()

                while (iterator.hasNext()) {
                    val key = iterator.next()
                    iterator.remove()

                    if (!key.isValid) {
                        continue
                    }

                    if (key.isReadable) {
                        buffer.clear()
                        channel.receive(buffer)
                        reaStreamPacket.readFromBuffer(buffer)
                        if (identifier == reaStreamPacket.getIdentifier()) {

                            if (reaStreamPacket.isAudioData) {

                                audioPacketHandler?.let { audioPacketHandler ->

                                    val channels = reaStreamPacket.channels.toInt()
                                    val sampleRate = reaStreamPacket.sampleRate
                                    val audioData = reaStreamPacket.audioData!!
                                    val audioDataLength = reaStreamPacket.blockLength / ReaStreamPacket.PER_SAMPLE_BYTES

                                    audioPacketHandler.process(channels, sampleRate, audioData, audioDataLength)
                                }

                            } else if (reaStreamPacket.isMidiData) {

                                midiPacketHandler?.let { midiPacketHandler ->
                                    reaStreamPacket.midiEvents!!.forEach { midiEvent ->
                                        midiPacketHandler.process(midiEvent)
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Close UDP channel.
     */
    override fun close() {
        closed = true
        channel.close()

        audioPacketHandler?.release()
        midiPacketHandler?.release()
    }
}

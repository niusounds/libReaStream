package com.eje_c.libreastream

import java.net.InetSocketAddress
import java.net.SocketException
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
        private val channel: DatagramChannel = DatagramChannel.open(),
        val identifier: String = ReaStream.DEFAULT_IDENTIFIER,
        port: Int = ReaStream.DEFAULT_PORT) : AutoCloseable {

    private val reaStreamPacket = ReaStreamPacket()
    private val buffer: ByteBuffer = ByteBuffer.allocate(ReaStreamPacket.MAX_BLOCK_LENGTH + ReaStreamPacket.AUDIO_PACKET_HEADER_BYTE_SIZE)
            .apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }
    private var closed: Boolean = false

    var onReaStreamPacketListener: OnReaStreamPacketListener? = null

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
                            onReaStreamPacketListener?.onReceive(reaStreamPacket)
                        }
                    }
                }
            }
        }

    }

    /**
     * Sets the read timeout in milliseconds for this channel.
     * This receive timeout defines the period the channel will block waiting to
     * receive data before throwing an `InterruptedIOException`. The value
     * `0` (default) is used to set an infinite timeout. To have effect
     * this option must be set before the blocking method was called.
     *
     * @param timeout the timeout in milliseconds or 0 for no timeout.
     * @throws SocketException if an error occurs while setting the option.
     */
    @Throws(SocketException::class)
    fun setTimeout(timeout: Int) {
        channel.socket().soTimeout = timeout
    }

    /**
     * Close UDP channel.
     */
    override fun close() {
        closed = true
        channel.close()
    }
}

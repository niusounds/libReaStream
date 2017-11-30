package com.eje_c.libreastream

import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.DatagramChannel

class ReaStreamReceiver(
        private val socket: DatagramChannel = DatagramChannel.open(),
        port: Int = DEFAULT_PORT) : AutoCloseable {

    private val reaStreamPacket = ReaStreamPacket()
    private val buffer: ByteBuffer = ByteBuffer.allocate(ReaStreamPacket.MAX_BLOCK_LENGTH + ReaStreamPacket.AUDIO_PACKET_HEADER_BYTE_SIZE)
            .apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }

    var identifier = DEFAULT_IDENTIFIER

    init {
        socket.socket().bind(InetSocketAddress(port))
    }

    /**
     * Wait for receiving [ReaStreamPacket].
     * This method blocks until receiving ReaStream packet with same identifier.
     *
     * @return Received ReaStream packet.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun receive(): ReaStreamPacket {

        do {
            buffer.clear()
            socket.receive(buffer)
            reaStreamPacket.readFromBuffer(buffer)
        } while (identifier != reaStreamPacket.getIdentifier())

        return reaStreamPacket
    }

    /**
     * Sets the read timeout in milliseconds for this socket.
     * This receive timeout defines the period the socket will block waiting to
     * receive data before throwing an `InterruptedIOException`. The value
     * `0` (default) is used to set an infinite timeout. To have effect
     * this option must be set before the blocking method was called.
     *
     * @param timeout the timeout in milliseconds or 0 for no timeout.
     * @throws SocketException if an error occurs while setting the option.
     */
    @Throws(SocketException::class)
    fun setTimeout(timeout: Int) {
        socket.socket().soTimeout = timeout
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

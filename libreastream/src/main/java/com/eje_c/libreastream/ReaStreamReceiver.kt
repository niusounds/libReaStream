package com.eje_c.libreastream

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ReaStreamReceiver
/**
 * Create ReaStream receiver
 *
 * @param socket Pre-created socket
 */
(private val socket: DatagramSocket) : AutoCloseable {
    private val packet: DatagramPacket
    private val audioPacket = ReaStreamPacket()
    private val buffer: ByteBuffer = ByteBuffer.allocate(ReaStreamPacket.MAX_BLOCK_LENGTH + ReaStreamPacket.AUDIO_PACKET_HEADER_BYTE_SIZE)
            .apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }

    var identifier = DEFAULT_IDENTIFIER

    /**
     * Create ReaStream receiver with UDP socket specific port.
     *
     * @param port UDP port
     * @throws SocketException
     */
    @Throws(SocketException::class)
    @JvmOverloads constructor(port: Int = DEFAULT_PORT) : this(DatagramSocket(port))

    init {
        packet = DatagramPacket(buffer.array(), buffer.capacity())
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
            socket.receive(packet)
            audioPacket.readFromBuffer(buffer)
        } while (identifier != audioPacket.getIdentifier())

        return audioPacket
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
        socket.soTimeout = timeout
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

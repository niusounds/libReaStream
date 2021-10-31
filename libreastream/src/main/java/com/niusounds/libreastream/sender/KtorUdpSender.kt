package com.niusounds.libreastream.sender

import com.niusounds.libreastream.ReaStream
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.ConnectedDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.aSocket
import io.ktor.util.network.NetworkAddress
import io.ktor.utils.io.core.ByteReadPacket
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Receive UDP data with Ktor.
 */
class KtorUdpSender(
    private val host: String,
    private val port: Int = ReaStream.DEFAULT_PORT,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) : PacketSender {
    private val client: ConnectedDatagramSocket by lazy {
        aSocket(ActorSelectorManager(ioContext))
            .udp()
            .connect(NetworkAddress(host, port))
    }

    override suspend fun send(data: ByteBuffer) = withContext(ioContext) {
        client.send(Datagram(ByteReadPacket(data), client.remoteAddress))
    }

    override fun close() {
        client.close()
    }
}

package com.niusounds.libreastream.sender

import com.niusounds.libreastream.ReaStream
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

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
}

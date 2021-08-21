package com.niusounds.libreastream.sender

import com.niusounds.libreastream.ReaStream
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

/**
 * Receive UDP data with Ktor.
 */
class KtorUdpSender(
    private val host: String,
    private val port: Int = ReaStream.DEFAULT_PORT,
    ioContext: CoroutineContext = Dispatchers.IO,
) : PacketSender {
    private val client = aSocket(ActorSelectorManager(ioContext))
        .udp()
        .connect(NetworkAddress(host, port))

    override suspend fun send(data: ByteBuffer) {
        client.send(Datagram(ByteReadPacket(data), NetworkAddress(host, port)))
    }
}

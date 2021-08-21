package com.niusounds.libreastream.receiver

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.CoroutineContext

/**
 * Receive UDP data with Ktor.
 */
class KtorUdpReceiver(
    private val port: Int,
    private val ioContext: CoroutineContext = Dispatchers.IO,
) : PacketReceiver {
    override fun receive(): Flow<ByteBuffer> {
        val server = aSocket(ActorSelectorManager(ioContext))
            .udp()
            .bind(InetSocketAddress(port))

        return server.incoming.consumeAsFlow()
            .map {
                it.packet.readByteBuffer().order(ByteOrder.LITTLE_ENDIAN)
            }
    }
}

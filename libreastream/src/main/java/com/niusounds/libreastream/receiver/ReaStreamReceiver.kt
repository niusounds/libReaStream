package com.niusounds.libreastream.receiver

import com.niusounds.libreastream.ReaStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.CoroutineContext

/**
 * Receive [ReaStreamPacket]s as [Flow].
 */
fun receiveReaStream(
    identifier: String? = null,
    receiver: PacketReceiver = KtorUdpReceiver(
        port = ReaStream.DEFAULT_PORT
    ),
): Flow<ReaStreamPacket> {
    val flow = receiver.receive().map { ByteBufferReaStreamPacket(it) }
    return if (identifier != null) {
        flow.filter { it.identifier == identifier }
    } else {
        flow
    }
}

/**
 * Start point for ReaStream receiver.
 * `identifier` must be the same with sender.
 * Default `receiver` uses UDP for transport layer which is compatible with REAPER's ReaStream plugin.
 * Alternative implementation [DatagramSocketReceiver] is also available.
 */
@Deprecated("Use receiveReaStream and share Flow as needed.")
class ReaStreamReceiver(
    identifier: String = ReaStream.DEFAULT_IDENTIFIER,
    receiver: PacketReceiver = KtorUdpReceiver(ReaStream.DEFAULT_PORT),
    scope: CoroutineScope = GlobalScope,
    context: CoroutineContext = Dispatchers.IO,
) {
    private val converter = ReaStreamFlowConverter(identifier, receiver)

    val packets: Flow<ReaStreamPacket> by lazy {
        converter.receive()
            .flowOn(context)
            .shareIn(scope, SharingStarted.WhileSubscribed())
    }
}

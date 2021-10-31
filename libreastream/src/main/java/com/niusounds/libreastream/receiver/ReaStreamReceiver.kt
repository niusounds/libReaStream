package com.niusounds.libreastream.receiver

import com.niusounds.libreastream.ReaStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

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

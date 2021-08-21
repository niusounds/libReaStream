package com.niusounds.libreastream.receiver

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Deprecated("Use receiveReaStream.")
class ReaStreamFlowConverter(
    private val identifier: String,
    private val receiver: PacketReceiver,
) {
    fun receive(): Flow<ReaStreamPacket> {
        return receiver.receive()
            .map {
                ByteBufferReaStreamPacket(it)
            }
            .filter { it.identifier == identifier }
    }
}

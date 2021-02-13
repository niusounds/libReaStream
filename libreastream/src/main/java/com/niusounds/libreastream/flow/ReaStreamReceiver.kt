package com.niusounds.libreastream.flow

import com.niusounds.libreastream.ReaStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlin.coroutines.CoroutineContext

/**
 * Start point for ReaStream receiver.
 * `identifier` must be the same with sender.
 * Default `receiver` uses UDP for transport layer which is compatible with REAPER's ReaStream plugin.
 * Alternative implementation [DatagramSocketReceiver] is also available.
 */
class ReaStreamReceiver(
    identifier: String = ReaStream.DEFAULT_IDENTIFIER,
    receiver: PacketReceiver = DatagramChannelReceiver(ReaStream.DEFAULT_PORT, 65535),
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

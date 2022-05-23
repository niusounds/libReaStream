package com.niusounds.libreastream.receiver

import kotlinx.coroutines.flow.Flow
import java.nio.ByteBuffer

interface PacketReceiver {
    fun receive(): Flow<ByteBuffer>
}

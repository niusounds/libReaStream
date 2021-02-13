package com.niusounds.libreastream.flow

import kotlinx.coroutines.flow.Flow
import java.nio.ByteBuffer

interface PacketReceiver {
    fun receive(): Flow<ByteBuffer>
}
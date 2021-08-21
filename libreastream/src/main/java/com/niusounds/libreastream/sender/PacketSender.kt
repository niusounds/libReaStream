package com.niusounds.libreastream.sender

import java.nio.ByteBuffer

interface PacketSender {
    suspend fun send(data: ByteBuffer)
}
package com.niusounds.libreastream.flow

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

class DatagramSocketReceiver(
    private val port: Int,
    private val bufferSize: Int,
    private val bufferCount: Int = 64,
) : PacketReceiver {
    private class BufferBlock(
        val byteBuffer: ByteBuffer,
        val packet: DatagramPacket,
    ) {
        fun prepare() {
            byteBuffer.position(0).limit(packet.length)
        }
    }

    @ExperimentalCoroutinesApi
    override fun receive() = callbackFlow {
        val udpThread = thread {
            val bufferBlocks = Array(bufferCount) {
                val buffer = ByteArray(bufferSize)
                val byteBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                val packet = DatagramPacket(buffer, bufferSize)
                BufferBlock(byteBuffer, packet)
            }
            val socket = DatagramSocket(port)

            try {
                var i = 0
                while (!Thread.interrupted()) {
                    val bufferBlock = bufferBlocks[i]
                    socket.receive(bufferBlock.packet)
                    bufferBlock.prepare()
                    offer(bufferBlock.byteBuffer)
                    i++
                    if (i >= bufferBlocks.size) {
                        i = 0
                    }
                }
            } catch (e: Exception) {
                Log.e("DatagramSocketReceiver", "error", e)
            }

            socket.close()
        }

        awaitClose {
            udpThread.interrupt()
        }
    }
}

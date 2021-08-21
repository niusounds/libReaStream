package com.niusounds.libreastream.receiver

import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import kotlin.concurrent.thread

@Deprecated("KtorUdpReceiver is recommended implementation")
class DatagramChannelReceiver(
    private val port: Int,
    private val bufferSize: Int,
    private val bufferCount: Int = 64,
) : PacketReceiver {
    @ExperimentalCoroutinesApi
    override fun receive() = callbackFlow<ByteBuffer> {
        val udpThread = thread {
            val buffers = Array(bufferCount) {
                ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN)
            }
            val channel = DatagramChannel.open().apply {
                socket().bind(InetSocketAddress(port))
                configureBlocking(false)
            }

            val selector = Selector.open()
            channel.register(selector, SelectionKey.OP_READ)

            try {
                var i = 0
                while (!Thread.interrupted()) {
                    selector.select()
                    val iterator = selector.selectedKeys().iterator()

                    while (iterator.hasNext()) {
                        val key = iterator.next()
                        iterator.remove()

                        if (!key.isValid) {
                            continue
                        }

                        if (key.isReadable) {
                            val buffer = buffers[i]
                            buffer.clear()
                            channel.receive(buffer)
                            trySend(buffer)
                            i++
                            if (i >= buffers.size) {
                                i = 0
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DatagramChannelReceiver", "error", e)
            }

            channel.close()
        }

        awaitClose {
            udpThread.interrupt()
        }
    }
}

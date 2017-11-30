package com.eje_c.libreastream

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.io.IOException
import java.net.SocketException
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ReaStreamReceiverService : Service() {

    private val localBinder = LocalBinder()
    private val executorService = Executors.newSingleThreadExecutor()
    private var receiver: ReaStreamReceiver? = null // Non null while receiving
    private var future: Future<*>? = null
    private var identifier: String? = null
    private var timeout: Int = 0

    var isEnabled = true
    var onReaStreamPacketListener: OnReaStreamPacketListener? = null

    val isReceiving: Boolean
        get() = future != null

    private val receiverTask = {
        try {
            ReaStreamReceiver(identifier = identifier ?: ReaStream.DEFAULT_IDENTIFIER).use { receiver ->
                this@ReaStreamReceiverService.receiver = receiver

                receiver.setTimeout(timeout)

                while (!Thread.interrupted()) {
                    if (isEnabled) {
                        val packet = receiver.receive()
                        if (onReaStreamPacketListener != null) {
                            onReaStreamPacketListener!!.onReceive(packet)
                        }
                    }
                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            this@ReaStreamReceiverService.receiver = null
        }
    }

    inner class LocalBinder : Binder() {
        val service: ReaStreamReceiverService
            get() = this@ReaStreamReceiverService
    }

    interface OnReaStreamPacketListener {
        fun onReceive(packet: ReaStreamPacket)
    }

    override fun onBind(intent: Intent): IBinder? {
        return localBinder
    }

    fun startReveiving() {

        if (future == null) {
            future = executorService.submit(receiverTask)
        }
    }

    fun stopReceiving() {

        if (future != null) {
            future!!.cancel(true)
            future = null
        }
    }

    fun setIdentifier(identifier: String) {
        this.identifier = identifier
    }

    @Throws(SocketException::class)
    fun setTimeout(timeout: Int) {
        this.timeout = timeout
        receiver?.setTimeout(timeout)
    }

    override fun onDestroy() {

        stopReceiving()
        receiver?.close()

        super.onDestroy()
    }
}

package com.eje_c.libreastream

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.net.SocketException

class ReaStreamReceiverService : Service() {

    private val localBinder = LocalBinder()
    private var receiver: ReaStreamReceiver? = null // Non null while receiving
    private var identifier: String? = null
    private var timeout: Int = 0

    var isEnabled = true
    var onReaStreamPacketListener: OnReaStreamPacketListener? = null

    var isReceiving: Boolean = false

    inner class LocalBinder : Binder() {
        val service: ReaStreamReceiverService
            get() = this@ReaStreamReceiverService
    }

    override fun onBind(intent: Intent): IBinder? {
        return localBinder
    }

    fun startReceiving() {

        if (!isReceiving) {

            receiver = ReaStreamReceiver(identifier = identifier ?: ReaStream.DEFAULT_IDENTIFIER)
            receiver?.setTimeout(timeout)
            receiver?.onReaStreamPacketListener = onReaStreamPacketListener

            isReceiving = true
        }
    }

    fun stopReceiving() {

        if (isReceiving) {
            receiver?.close()
            isReceiving = false
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

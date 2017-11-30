package com.eje_c.reastream

import android.annotation.SuppressLint
import com.eje_c.libreastream.*
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException

class ReaStream(val sampleRate: Int = 44100) : AutoCloseable {

    var isSending: Boolean = false
        private set

    var isReceiving: Boolean = false
        private set

    var isEnabled = true

    var onMidiEvents: ((Array<MidiEvent>) -> Unit)? = null

    private var sender: ReaStreamSender? = null     // Non null while sending
    private var receiver: ReaStreamReceiver? = null // Non null while receiving
    private var remoteAddress: InetAddress? = null

    /**
     * Start sending audio from microphone.
     */
    fun startSending() {

        if (!isSending) {
            isSending = true
            SenderThread().start()
        }
    }

    /**
     * Stop sending audio from microphone.
     */
    fun stopSending() {
        isSending = false
    }

    /**
     * Start receiving audio.
     */
    fun startReveiving() {

        if (!isReceiving) {
            isReceiving = true
            ReceiverThread().start()
        }
    }

    /**
     * Stop receiving audio.
     */
    fun stopReceiving() {
        isReceiving = false
    }

    /**
     * Set identifier. Identifier is used to identify audio stream.
     */
    fun setIdentifier(identifier: String) {

        if (sender != null) {
            sender!!.identifier = identifier
        }

        if (receiver != null) {
            receiver!!.identifier = identifier
        }
    }

    @Throws(UnknownHostException::class)
    fun setRemoteAddress(remoteAddress: String) {
        setRemoteAddress(InetAddress.getByName(remoteAddress))
    }

    fun setRemoteAddress(remoteAddress: InetAddress) {
        this.remoteAddress = remoteAddress

        if (sender != null) {
            sender!!.remoteAddress = remoteAddress
        }
    }

    override fun close() {
        sender?.close()
        receiver?.close()
    }

    private inner class SenderThread : Thread() {

        override fun run() {

            try {
                ReaStreamSender().use { sender ->
                    AudioRecordSrc(sampleRate).use { audioRecordSrc ->

                        audioRecordSrc.start()
                        this@ReaStream.sender = sender

                        sender.sampleRate = sampleRate
                        sender.channels = 1.toByte()
                        sender.remoteAddress = remoteAddress!!

                        while (isSending) {

                            // Read from mic and send it
                            val buffer = audioRecordSrc.read()
                            val readCount = buffer.limit()
                            if (isEnabled && readCount > 0) {
                                sender.send(buffer.array(), readCount)
                            }
                        }

                        audioRecordSrc.stop()

                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                this@ReaStream.sender = null
            }
        }
    }

    private inner class ReceiverThread : Thread() {

        @SuppressLint("NewApi")
        override fun run() {

            try {
                ReaStreamReceiver().use { receiver ->
                    AudioTrackSink(sampleRate).use { audioTrackSink ->
                        this@ReaStream.receiver = receiver

                        while (isReceiving) {
                            if (isEnabled) {
                                val packet = receiver.receive()
                                if (packet.isAudioData) {
                                    audioTrackSink.onReceive(packet)
                                } else if (packet.isMidiData) {
                                    onMidiEvents?.invoke(packet.midiEvents!!)
                                }
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                this@ReaStream.receiver = null
            }
        }
    }
}

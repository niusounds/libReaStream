package com.eje_c.libreastream

import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException
import kotlin.concurrent.thread

/**
 * Main entry class for libReaStream.
 * @param sampleRate Sample rate for input and output.
 * @param identifier Identifier to identify ReaStream packet.
 * @param port Port number for waiting remote packet and sending to remote.
 */
class ReaStream(
        val sampleRate: Int = DEFAULT_SAMPLE_RATE,
        val identifier: String = DEFAULT_IDENTIFIER,
        val port: Int = DEFAULT_PORT) : AutoCloseable {

    var isSending: Boolean = false
        private set

    var isReceiving: Boolean = false
        private set

    var isEnabled = true

    var onMidiEvents: ((Array<MidiEvent>) -> Unit)? = null

    private var sender: ReaStreamSender? = null     // Non null while sending
    private var receiver: ReaStreamReceiver? = null // Non null while receiving
    private var audioTrackSink: AudioTrackSink? = null // Non null while receiving

    var remoteAddress: InetAddress? = null
        set(value) {
            field = value

            // Restart sending thread
            if (isSending) {
                stopSending()
                thread {
                    Thread.sleep(100)
                    startSending()
                }
            }
        }

    /**
     * Start sending audio from microphone.
     */
    fun startSending() {

        if (!isSending) {
            isSending = true

            // Start new thread to send audio
            thread {

                try {
                    ReaStreamSender(
                            remote = InetSocketAddress(remoteAddress, port),
                            identifier = identifier
                    ).use { sender ->
                        AudioRecordSrc(sampleRate).use { audioRecordSrc ->

                            audioRecordSrc.start()
                            this@ReaStream.sender = sender

                            sender.sampleRate = sampleRate
                            sender.channels = 1.toByte()

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
    fun startReceiving() {

        if (!isReceiving) {
            isReceiving = true

            receiver = ReaStreamReceiver(
                    identifier = identifier,
                    port = port
            )

            audioTrackSink = AudioTrackSink(sampleRate)

            receiver?.onReaStreamPacketListener = object : OnReaStreamPacketListener {
                override fun onReceive(packet: ReaStreamPacket) {
                    if (packet.isAudioData) {
                        audioTrackSink?.onReceive(packet)
                    } else if (packet.isMidiData) {
                        onMidiEvents?.invoke(packet.midiEvents!!)
                    }
                }
            }

        }
    }

    /**
     * Stop receiving audio.
     */
    fun stopReceiving() {
        isReceiving = false
    }

    @Throws(UnknownHostException::class)
    fun setRemoteAddress(remoteAddress: String) {
        this.remoteAddress = InetAddress.getByName(remoteAddress)
    }

    override fun close() {
        sender?.close()
        receiver?.close()
        audioTrackSink?.close()
    }

    companion object {
        const val DEFAULT_SAMPLE_RATE = 44100
        const val DEFAULT_PORT = 58710
        const val DEFAULT_IDENTIFIER = "default"
    }
}

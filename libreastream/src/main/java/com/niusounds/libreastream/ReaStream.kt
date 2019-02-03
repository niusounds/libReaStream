package com.niusounds.libreastream

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
        var identifier: String = DEFAULT_IDENTIFIER,
        val port: Int = DEFAULT_PORT,
        val audioStreamSourceFactory: AudioStreamSource.Factory = AudioRecord.Factory(sampleRate),
        val audioPacketHandlerFactory: AudioPacketHandler.Factory = AudioTrackSink.Factory(sampleRate),
        val midiPacketHandlerFactory: MidiPacketHandler.Factory? = null) : AutoCloseable {

    var isSending: Boolean = false
        private set

    var isReceiving: Boolean = false
        private set

    var isEnabled = true

    private var sender: ReaStreamSender? = null     // Non null while sending
    private var receiver: ReaStreamReceiver? = null // Non null while receiving

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

                val streamSource = audioStreamSourceFactory.create()

                try {
                    ReaStreamSender(
                            remote = InetSocketAddress(remoteAddress, port),
                            identifier = identifier
                    ).use { sender ->
                        this@ReaStream.sender = sender

                        sender.sampleRate = sampleRate
                        sender.channels = 1.toByte()

                        while (isSending) {

                            // Read from mic and send it
                            val buffer = streamSource.read()
                            val readCount = buffer.limit()
                            if (isEnabled && readCount > 0) {
                                sender.send(buffer.array(), readCount)
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    streamSource.release()
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
                    port = port,
                    audioPacketHandler = audioPacketHandlerFactory.create(),
                    midiPacketHandler = midiPacketHandlerFactory?.create()
            )

        }
    }

    /**
     * Stop receiving audio.
     */
    fun stopReceiving() {
        isReceiving = false

        receiver?.close()
        receiver = null
    }

    @Throws(UnknownHostException::class)
    fun setRemoteAddress(remoteAddress: String) {
        this.remoteAddress = InetAddress.getByName(remoteAddress)
    }

    override fun close() {
        sender?.close()
        receiver?.close()
    }

    companion object {
        const val DEFAULT_SAMPLE_RATE = 44100
        const val DEFAULT_PORT = 58710
        const val DEFAULT_IDENTIFIER = "default"
    }
}

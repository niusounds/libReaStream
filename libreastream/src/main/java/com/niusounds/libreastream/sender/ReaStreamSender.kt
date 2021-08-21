package com.niusounds.libreastream.sender

import com.niusounds.libreastream.ReaStream

fun ReaStreamSender(
    identifier: String,
    sampleRate: Int,
    channels: Int,
    remoteHost: String,
    port: Int = ReaStream.DEFAULT_PORT,
): ReaStreamSender {
    return ReaStreamSender(
        identifier = identifier,
        sampleRate = sampleRate,
        channels = channels,
        sender = KtorUdpSender(
            host = remoteHost,
            port = port,
        )
    )
}

class ReaStreamSender(
    identifier: String,
    sampleRate: Int,
    channels: Int,
    private val sender: PacketSender,
) {
    private val serializer = AudioDataSerializer(
        identifier = identifier,
        sampleRate = sampleRate,
        channels = channels,
    )
    private val midiSerializer = MidiDataSerializer(
        identifier = identifier,
    )

    suspend fun send(audioData: FloatArray) {
        val bytes = serializer.toByteBuffer(audioData)
        sender.send(bytes)
    }

    suspend fun send(midiData: ByteArray) {
        val bytes = midiSerializer.toByteBuffer(midiData = midiData)
        sender.send(bytes)
    }
}
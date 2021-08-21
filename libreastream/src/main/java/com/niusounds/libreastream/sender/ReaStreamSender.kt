package com.niusounds.libreastream.sender

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

    suspend fun send(audioData: FloatArray) {
        val bytes = serializer.toByteBuffer(audioData)
        sender.send(bytes)
    }
}
package com.eje_c.libreastream

interface AudioPacketHandler {

    /**
     * Process audio data.
     */
    fun process(channels: Int, sampleRate: Int, audioData: FloatArray, audioDataLength: Int)

    /**
     * Release resources.
     */
    fun release()

    interface Factory {
        fun create(): AudioPacketHandler
    }
}
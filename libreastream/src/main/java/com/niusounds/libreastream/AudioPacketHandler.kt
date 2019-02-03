package com.niusounds.libreastream

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
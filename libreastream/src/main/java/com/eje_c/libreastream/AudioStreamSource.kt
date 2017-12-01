package com.eje_c.libreastream

import java.nio.FloatBuffer

interface AudioStreamSource {

    /**
     * Read audio data and return it.
     * Returned FloatBuffer must be filled with audio data starting at position 0 and set valid audio data length to [FloatBuffer.limit].
     */
    fun read(): FloatBuffer

    /**
     * Release resources.
     */
    fun release()

    interface Factory {
        fun create(): AudioStreamSource
    }
}
package com.niusounds.libreastream

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.max

/**
 * Simple wrapper for [AudioTrack].
 */
class AudioTrack(sampleRate: Int = ReaStream.DEFAULT_SAMPLE_RATE) : AutoCloseable {
    private val track: AudioTrack

    init {
        val bufferSize = max(
            AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_FLOAT
            ),
            ReaStreamPacket.MAX_BLOCK_LENGTH * Float.SIZE_BYTES
        )
        track = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_FLOAT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
        track.play()
    }

    override fun close() {
        track.stop()
        track.release()
    }

    /**
     * Write audio data.
     * @param data Audio data.
     * @param offsetInFloats Start offset of audio data. Default is 0.
     * @param sizeInFloats Size of audio data. Default is all for rest of audio data.
     * @param writeMode Write mode. Default is non-blocking async mode.
     */
    fun write(
        data: FloatArray,
        offsetInFloats: Int = 0,
        sizeInFloats: Int = data.size - offsetInFloats,
        writeMode: Int = AudioTrack.WRITE_NON_BLOCKING
    ) {
        track.write(data, offsetInFloats, sizeInFloats, writeMode)
    }
}

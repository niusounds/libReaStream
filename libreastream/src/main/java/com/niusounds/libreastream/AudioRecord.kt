package com.niusounds.libreastream

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build

import java.nio.FloatBuffer

/**
 * Simple wrapper for [AudioRecord].
 */
class AudioRecord(sampleRate: Int = ReaStream.DEFAULT_SAMPLE_RATE) : AudioStreamSource {

    private val record: AudioRecord
    private val floatBuffer: FloatBuffer
    private val audioReader: AudioReader
    private val bufferSize: Int

    init {
        val channel = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = if (isAndroidM) AudioFormat.ENCODING_PCM_FLOAT else AudioFormat.ENCODING_PCM_16BIT
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat)
        record = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, audioFormat, bufferSize)
        record.startRecording()

        // Prepare float buffer
        floatBuffer = FloatBuffer.allocate(bufferSize / 4)

        audioReader = if (isAndroidM) AudioReaderM() else AudioReaderPriorM()
    }

    override fun release() {
        record.stop()
        record.release()
    }

    /**
     * Read audio data from [AudioRecord] in float format and return it.
     *
     * @return Audio data FloatBuffer.
     */
    override fun read(): FloatBuffer {

        val readCount = audioReader.read()

        floatBuffer.limit(readCount)

        return floatBuffer
    }

    interface AudioReader {
        fun read(): Int
    }

    private inner class AudioReaderM : AudioReader {

        val recordBuffer: FloatArray = floatBuffer.array()

        @SuppressLint("NewApi")
        override fun read(): Int {
            return record.read(recordBuffer, 0, recordBuffer.size, AudioRecord.READ_NON_BLOCKING)
        }
    }

    private inner class AudioReaderPriorM : AudioReader {
        val shortBuffer = ShortArray(bufferSize / 4)

        override fun read(): Int {
            val readCount = record.read(shortBuffer, 0, shortBuffer.size)

            // Convert short[] to float[]
            floatBuffer.position(0)

            for (i in 0 until readCount) {
                val f = shortBuffer[i].toFloat() * SHORT_TO_FLOAT
                floatBuffer.put(f)
            }

            return readCount
        }
    }

    companion object {
        private val SHORT_TO_FLOAT = 1.0f / Short.MAX_VALUE

        private val isAndroidM: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    class Factory(val sampleRate: Int = ReaStream.DEFAULT_SAMPLE_RATE) : AudioStreamSource.Factory {
        override fun create(): AudioStreamSource {
            return AudioRecord(sampleRate = sampleRate)
        }
    }
}

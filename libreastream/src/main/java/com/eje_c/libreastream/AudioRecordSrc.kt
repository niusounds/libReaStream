package com.eje_c.libreastream

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build

import java.nio.FloatBuffer

class AudioRecordSrc(sampleRate: Int) : AutoCloseable {
    private val record: AudioRecord
    private val recordBuffer: FloatArray
    private val floatBuffer: FloatBuffer
    private val shortBuffer: ShortArray?

    init {
        val channel = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = if (isAndroidM) AudioFormat.ENCODING_PCM_FLOAT else AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat)
        record = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, audioFormat, bufferSize)
        floatBuffer = FloatBuffer.allocate(bufferSize / 4)
        recordBuffer = floatBuffer.array()

        if (!isAndroidM) {
            shortBuffer = ShortArray(bufferSize / 4)
        } else {
            shortBuffer = null
        }
    }


    /**
     * Must call this before first [.read].
     */
    fun start() {

        if (record.recordingState == AudioRecord.RECORDSTATE_STOPPED) {
            record.startRecording()
        }
    }

    fun stop() {

        if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            record.stop()
        }
    }

    override fun close() {
        record.release()
    }

    /**
     * Read audio data from [android.media.AudioTrack] and return it.
     *
     * @return Audio data FloatBuffer.
     */
    @SuppressLint("NewApi")
    fun read(): FloatBuffer {

        if (isAndroidM) {
            val readCount = record.read(recordBuffer, 0, recordBuffer.size, AudioRecord.READ_NON_BLOCKING)
            floatBuffer.limit(readCount)
        } else {
            val readCount = record.read(shortBuffer!!, 0, shortBuffer.size)

            // Convert short[] to float[]
            floatBuffer.position(0)

            for (i in 0 until readCount) {
                val f = shortBuffer[i].toFloat() * SHORT_TO_FLOAT
                floatBuffer.put(f)
            }

            floatBuffer.limit(readCount)
        }

        return floatBuffer
    }

    companion object {
        private val SHORT_TO_FLOAT = 1.0f / java.lang.Short.MAX_VALUE

        private val isAndroidM: Boolean
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
}

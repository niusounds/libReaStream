package com.niusounds.libreastream.sender

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.niusounds.libreastream.receiver.ReaStreamPacket
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

/**
 * To use this class, RECORD_AUDIO permission is required.
 */
class AudioRecordInput(
    private val sampleRate: Int,
    private val channels: Int,
    private val chunkSize: Int = ReaStreamPacket.MAX_BLOCK_LENGTH / Float.SIZE_BYTES,
) {
    @SuppressLint("MissingPermission")
    fun readAudio(): Flow<FloatArray> = flow {
        val audioSource = MediaRecorder.AudioSource.DEFAULT
        val channelConfig = when (channels) {
            1 -> AudioFormat.CHANNEL_IN_MONO
            2 -> AudioFormat.CHANNEL_IN_STEREO
            else -> error("unsupported channels")
        }
        val audioFormat = AudioFormat.ENCODING_PCM_FLOAT
        val bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        val record = AudioRecord.Builder()
            .setAudioSource(audioSource)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(audioFormat)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(bufferSizeInBytes)
            .build()

        record.startRecording()

        try {
            val audioData = FloatArray(chunkSize)
            while (currentCoroutineContext().isActive) {
                record.read(audioData, 0, chunkSize, AudioRecord.READ_BLOCKING)
                emit(audioData)
            }
        } finally {
            record.stop()
            record.release()
        }
    }
}
package com.niusounds.libreastream.sender

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import com.niusounds.libreastream.receiver.ReaStreamPacket
import kotlin.concurrent.thread
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * To use this class, RECORD_AUDIO permission is required.
 */
class AudioRecordInput(
    private val sampleRate: Int,
    private val channels: Int,
    private val chunkSize: Int = ReaStreamPacket.MAX_BLOCK_LENGTH / Float.SIZE_BYTES,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    fun readAudio(): Flow<FloatArray> = callbackFlow {
        val audioThread = thread {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)

            val audioSource = MediaRecorder.AudioSource.DEFAULT
            val channelConfig = when (channels) {
                1 -> AudioFormat.CHANNEL_IN_MONO
                2 -> AudioFormat.CHANNEL_IN_STEREO
                else -> error("unsupported channels")
            }
            val audioFormat = AudioFormat.ENCODING_PCM_FLOAT
            val bufferSizeInBytes = AudioRecord.getMinBufferSize(
                sampleRate,
                channelConfig,
                audioFormat,
            )

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
                while (!Thread.interrupted()) {
                    val audioData = FloatArray(chunkSize)
                    record.read(audioData, 0, chunkSize, AudioRecord.READ_BLOCKING)
                    if (trySend(audioData).isClosed) {
                        break
                    }
                }
            } finally {
                record.stop()
                record.release()
            }
        }

        awaitClose { audioThread.interrupt() }
    }
}

/**
 * Interleaved audio data to Non-Interleaved audio data.
 */
fun FloatArray.deInterleave(channels: Int): FloatArray {
    val result = FloatArray(size)
    val perChannelSize = size / channels

    forEachIndexed { index, sample ->
        val ch = index % channels
        val i = index / channels
        result[perChannelSize * ch + i] = sample
    }

    return result
}

/**
 * Non-Interleaved audio data to Interleaved audio data.
 */
fun FloatArray.interleaved(channels: Int, length: Int = size): FloatArray {
    val result = FloatArray(length)
    val samplesPerChannel = length / channels

    repeat(samplesPerChannel) { i ->
        repeat(channels) { ch ->
            result[i * channels + ch] = get(samplesPerChannel * ch + i)
        }
    }

    return result
}
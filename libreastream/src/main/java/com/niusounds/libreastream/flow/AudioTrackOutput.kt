package com.niusounds.libreastream.flow

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.niusounds.libreastream.ReaStream
import com.niusounds.libreastream.getInterleavedAudioData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

/**
 * Output [ReaStreamPacket] audio data to device's speaker.
 * [sampleRate] must be the same with ReaStream sender. Re-sampling is not supported.
 * Recommend to use same [channels] with ReaStream sender.
 */
class AudioTrackOutput(
    private val sampleRate: Int = ReaStream.DEFAULT_SAMPLE_RATE,
    private val channels: Int = 2,
) {
    suspend fun play(flow: Flow<ReaStreamPacket>) {
        val channelMask = when (channels) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            else -> throw IllegalStateException("unsupported channels")
        }
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            channelMask,
            AudioFormat.ENCODING_PCM_FLOAT
        ).coerceAtLeast(ReaStreamPacket.MAX_BLOCK_LENGTH * Float.SIZE_BYTES)

        val track = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioTrack.Builder()
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .setBufferSizeInBytes(bufferSize)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setChannelMask(channelMask)
                        .setSampleRate(sampleRate)
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .build()
                )
                .build()
        } else {
            AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelMask,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize,
                AudioTrack.MODE_STREAM,
            )
        }
        track.play()

        val audioData = FloatArray(ReaStreamPacket.MAX_BLOCK_LENGTH * 2)
        val convertedSamples = FloatArray(ReaStreamPacket.MAX_BLOCK_LENGTH * 2)

        flow.filter { it.isAudio && it.sampleRate == sampleRate }
            .onEach {

                val channels = it.channels.toInt()
                val audioDataLength = it.readAudio(audioData)

                when (channels) {
                    2 -> {
                        if (this.channels == 2) {
                            // Interleave samples
                            // [left-s1, left-s2, left-s3, ..., left-sN, right-s1, right-s2, right-s3, ..., right-sN]
                            // -> [left-s1, right-s1, left-s2, right-s2, left-s3, right-s3, ..., left-sN, right-sN]
                            getInterleavedAudioData(
                                audioData,
                                audioDataLength,
                                channels,
                                convertedSamples,
                            )
                            track.write(
                                convertedSamples,
                                0,
                                audioDataLength,
                                AudioTrack.WRITE_NON_BLOCKING
                            )
                        } else {
                            // Down-mix stereo -> mono
                            val audioDataLengthMono = audioDataLength / channels
                            for (i in 0 until audioDataLengthMono) {
                                var sample = 0f
                                for (ch in 0 until channels) {
                                    sample += audioData[ch * audioDataLengthMono + i]
                                }
                                convertedSamples[i] = sample / channels
                            }
                            track.write(
                                convertedSamples,
                                0,
                                audioDataLengthMono,
                                AudioTrack.WRITE_NON_BLOCKING
                            )
                        }
                    }
                    1 -> {
                        if (this.channels == 1) {
                            track.write(
                                audioData,
                                0,
                                audioDataLength,
                                AudioTrack.WRITE_NON_BLOCKING
                            )
                        } else {
                            // Convert mono -> stereo
                            // [s1, s2, s3, ...] -> [left-s1, right-s1, left-s2, right-s2, left-s3, right-s3, ...]
                            for (i in 0 until audioDataLength) {
                                val baseIndex = i * 2
                                convertedSamples[baseIndex + 1] = audioData[i]
                                convertedSamples[baseIndex] = audioData[i]
                            }
                            track.write(
                                convertedSamples,
                                0,
                                audioDataLength * 2,
                                AudioTrack.WRITE_NON_BLOCKING
                            )
                        }
                    }
                }
            }
            .onCompletion { track.release() }
            .collect()
    }
}

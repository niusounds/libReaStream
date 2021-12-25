package com.niusounds.libreastream.receiver

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.niusounds.libreastream.resampler.R8brainFreeSrc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onCompletion

private fun defaultAudioTrackFactory(
    sampleRate: Int,
    channelMask: Int,
    bufferSize: Int
): AudioTrack {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
}

suspend fun Flow<ReaStreamPacket>.play(
    sampleRate: Int,
    channels: Int = 2,
    bufferScaleFactor: Int = 4,
    audioTrackFactory: (sampleRate: Int, channelMask: Int, bufferSize: Int) -> AudioTrack = ::defaultAudioTrackFactory,
) {
    val channelMask = when (channels) {
        1 -> AudioFormat.CHANNEL_OUT_MONO
        2 -> AudioFormat.CHANNEL_OUT_STEREO
        else -> error("unsupported channels")
    }
    val bufferSize = bufferScaleFactor * AudioTrack.getMinBufferSize(
        sampleRate,
        channelMask,
        AudioFormat.ENCODING_PCM_FLOAT
    ).coerceAtLeast(ReaStreamPacket.MAX_BLOCK_LENGTH * Float.SIZE_BYTES)

    val track = audioTrackFactory(sampleRate, channelMask, bufferSize)
    track.play()

    val audioData = FloatArray(ReaStreamPacket.MAX_BLOCK_LENGTH * 2)
    val convertedSamples = FloatArray(ReaStreamPacket.MAX_BLOCK_LENGTH * 2)

    // cache resampler per sampleRate
    val resamplers = mutableMapOf<Int, R8brainFreeSrc>()
    val resampledAudioDataMap = mutableMapOf<Int, FloatArray>()

    filter { it.isAudio }
        .onCompletion { track.release() }
        .collect { packet ->

            val packetChannels = packet.channels.toInt()

            // Ensure packet.sampleRate to match to playing sampleRate
            val (audioData: FloatArray, audioDataLength: Int) = if (packet.sampleRate == sampleRate) {
                val audioDataLength = packet.readAudioInterleaved(audioData)
                Pair(audioData, audioDataLength)
            } else {
                // sample rate conversion
                val resampler = resamplers.getOrPut(packet.sampleRate) {
                    R8brainFreeSrc(
                        srcSampleRate = packet.sampleRate.toDouble(),
                        dstSampleRate = sampleRate.toDouble(),
                        maxInLen = ReaStreamPacket.MAX_BLOCK_LENGTH / ReaStreamPacket.PER_SAMPLE_BYTES,
                    )
                }
                val audioDataLength = packet.readAudioInterleaved(audioData)
                val resampledAudioData = resampledAudioDataMap.getOrPut(packet.sampleRate) {
                    FloatArray(resampler.getMaxOutLen())
                }
                val resampledAudioDataLength = resampler.process(
                    input = audioData,
                    len = audioDataLength,
                    output = resampledAudioData,
                )
                Pair(resampledAudioData, resampledAudioDataLength)
            }

            when (packetChannels) {
                2 -> {
                    if (channels == 2) {
                        track.write(
                            audioData,
                            0,
                            audioDataLength,
                            AudioTrack.WRITE_BLOCKING
                        )
                    } else {
                        // Down-mix stereo -> mono
                        val audioDataLengthMono = audioDataLength / packetChannels
                        val factor = 1f / packetChannels.toFloat()
                        repeat(audioDataLengthMono) { i ->
                            var sample = 0f
                            repeat(packetChannels) { ch ->
                                sample += audioData[ch + i * packetChannels] * factor
                            }
                            convertedSamples[i] = sample
                        }
                        track.write(
                            convertedSamples,
                            0,
                            audioDataLengthMono,
                            AudioTrack.WRITE_BLOCKING
                        )
                    }
                }
                1 -> {
                    if (channels == 1) {
                        track.write(
                            audioData,
                            0,
                            audioDataLength,
                            AudioTrack.WRITE_BLOCKING
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
                            AudioTrack.WRITE_BLOCKING
                        )
                    }
                }
            }
        }
}

package com.niusounds.libreastream.ktx

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

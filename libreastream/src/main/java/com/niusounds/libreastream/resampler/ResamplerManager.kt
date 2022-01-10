package com.niusounds.libreastream.resampler

class ResamplerManager(
    private val dstSampleRate: Int,
    private val maxInLen: Int,
) {
    // src sample rate, channel
    private val cache = mutableMapOf<Int, MutableMap<Int, Resampler>>()

    fun getResampler(srcSampleRate: Int, channel: Int): Resampler {
        val resamplersForSampleRate = cache.getOrPut(srcSampleRate) { mutableMapOf() }
        return resamplersForSampleRate.getOrPut(channel) {
            R8brainFreeSrc(
                srcSampleRate = srcSampleRate.toDouble(),
                dstSampleRate = dstSampleRate.toDouble(),
                maxInLen = maxInLen,
            )
        }
    }
}

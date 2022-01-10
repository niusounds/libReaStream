package com.niusounds.libreastream.resampler

interface Resampler {
    fun getMaxOutLen(): Int
    fun process(input: FloatArray, len: Int, output: FloatArray): Int
    fun release()
}
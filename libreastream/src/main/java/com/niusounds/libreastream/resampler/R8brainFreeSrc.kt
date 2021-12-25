package com.niusounds.libreastream.resampler

class R8brainFreeSrc(
    srcSampleRate: Double,
    dstSampleRate: Double,
    maxInLen: Int,
) {
    private val nativePtr: Long = initNative(srcSampleRate, dstSampleRate, maxInLen)

    fun release() = release(nativePtr)

    fun getLatency(): Int = getLatency(nativePtr)
    fun getLatencyFrac(): Double = getLatencyFrac(nativePtr)
    fun getMaxOutLen(): Int = getMaxOutLen(nativePtr)
    fun clear() = clear(nativePtr)
    fun getInLenBeforeOutStart(): Int = getInLenBeforeOutStart(nativePtr)
    fun process(
        input: DoubleArray,
        len: Int,
        output: DoubleArray,
    ): Int = process(nativePtr, input, len, output)

    fun process(
        input: FloatArray,
        len: Int,
        output: FloatArray,
    ): Int = processFloat(nativePtr, input, len, output)

    // JNI

    private external fun initNative(
        srcSampleRate: Double,
        dstSampleRate: Double,
        maxInLen: Int,
    ): Long

    private external fun release(nativePtr: Long)
    private external fun getLatency(nativePtr: Long): Int
    private external fun getLatencyFrac(nativePtr: Long): Double
    private external fun getMaxOutLen(nativePtr: Long): Int
    private external fun clear(nativePtr: Long)
    private external fun getInLenBeforeOutStart(nativePtr: Long): Int
    private external fun process(
        nativePtr: Long,
        input: DoubleArray,
        len: Int,
        output: DoubleArray,
    ): Int

    private external fun processFloat(
        nativePtr: Long,
        input: FloatArray,
        len: Int,
        output: FloatArray,
    ): Int

    companion object {
        init {
            System.loadLibrary("r8brain_free_src_jni")
        }
    }
}

#include <CDSPResampler.h>
#include <jni.h>

extern "C"
JNIEXPORT jlong JNICALL
Java_com_niusounds_libreastream_resampler_R8brainFreeSrc_initNative(
        JNIEnv *env,
        jobject thiz,
        jdouble srcSampleRate,
        jdouble dstSampleRate,
        jint maxInLen
) {
    return reinterpret_cast<jlong>(new r8b::CDSPResampler(srcSampleRate, dstSampleRate, maxInLen));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_niusounds_libreastream_resampler_R8brainFreeSrc_release(
        JNIEnv *env,
        jobject thiz,
        jlong nativePtr
) {
    auto *resampler = reinterpret_cast<r8b::CDSPResampler *>(nativePtr);
    delete resampler;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_niusounds_libreastream_resampler_R8brainFreeSrc_getLatency(
        JNIEnv *env,
        jobject thiz,
        jlong nativePtr
) {
    auto *resampler = reinterpret_cast<r8b::CDSPResampler *>(nativePtr);
    return resampler->getLatency();
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_niusounds_libreastream_resampler_R8brainFreeSrc_getLatencyFrac(
        JNIEnv *env,
        jobject thiz,
        jlong nativePtr
) {
    auto *resampler = reinterpret_cast<r8b::CDSPResampler *>(nativePtr);
    return resampler->getLatencyFrac();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_niusounds_libreastream_resampler_R8brainFreeSrc_getMaxOutLen(
        JNIEnv *env,
        jobject thiz,
        jlong nativePtr
) {
    auto *resampler = reinterpret_cast<r8b::CDSPResampler *>(nativePtr);
    return resampler->getMaxOutLen(0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_niusounds_libreastream_resampler_R8brainFreeSrc_clear(
        JNIEnv *env,
        jobject thiz,
        jlong nativePtr
) {
    auto *resampler = reinterpret_cast<r8b::CDSPResampler *>(nativePtr);
    resampler->clear();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_niusounds_libreastream_resampler_R8brainFreeSrc_getInLenBeforeOutStart(
        JNIEnv *env,
        jobject thiz,
        jlong nativePtr
) {
    auto *resampler = reinterpret_cast<r8b::CDSPResampler *>(nativePtr);
    return resampler->getInLenBeforeOutStart();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_niusounds_libreastream_resampler_R8brainFreeSrc_process(
        JNIEnv *env,
        jobject thiz,
        jlong nativePtr,
        jdoubleArray input,
        jint len,
        jdoubleArray output
) {
    auto *resampler = reinterpret_cast<r8b::CDSPResampler *>(nativePtr);

    jdouble *ip0 = env->GetDoubleArrayElements(input, nullptr);
    double *op0;
    int outputSamples = resampler->process(ip0, len, op0);

    env->SetDoubleArrayRegion(output, 0, outputSamples, op0);

    env->ReleaseDoubleArrayElements(input, ip0, 0);

    return outputSamples;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_niusounds_libreastream_resampler_R8brainFreeSrc_processFloat(
        JNIEnv *env,
        jobject thiz,
        jlong nativePtr,
        jfloatArray input,
        jint len,
        jfloatArray output
) {
    auto *resampler = reinterpret_cast<r8b::CDSPResampler *>(nativePtr);

    jfloat *ip0f = env->GetFloatArrayElements(input, nullptr);
    jfloat *op0f = env->GetFloatArrayElements(output, nullptr);

    double ip0[len];
    for (int i = 0; i < len; ++i) {
        ip0[i] = ip0f[i];
    }

    double *op0;
    int outputSamples = resampler->process(ip0, len, op0);

    for (int i = 0; i < outputSamples; ++i) {
        op0f[i] = (float) op0[i];
    }

    env->ReleaseFloatArrayElements(input, ip0f, 0);
    env->ReleaseFloatArrayElements(output, op0f, 0);

    return outputSamples;
}

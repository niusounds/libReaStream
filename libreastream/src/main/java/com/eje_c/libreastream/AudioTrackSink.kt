package com.eje_c.libreastream

class AudioTrackSink(sampleRate: Int = ReaStream.DEFAULT_SAMPLE_RATE) : AudioPacketHandler {

    private val track = AudioTrack(sampleRate)
    private var convertedSamples: FloatArray? = null

    override fun process(channels: Int, sampleRate: Int, audioData: FloatArray, audioDataLength: Int) {

        if (channels == 2) {

            // Interleave samples
            // [left-s1, left-s2, left-s3, ..., left-sN, right-s1, right-s2, right-s3, ..., right-sN]
            // -> [left-s1, right-s1, left-s2, right-s2, left-s3, right-s3, ..., left-sN, right-sN]

            if (convertedSamples == null || convertedSamples!!.size < audioDataLength) {
                convertedSamples = FloatArray(audioDataLength)
            }

            getInterleavedAudioData(audioData, audioDataLength, channels, convertedSamples!!)

            track.write(convertedSamples!!, sizeInFloats = audioDataLength)

        } else if (channels == 1) {

            // Convert mono -> stereo
            // [s1, s2, s3, ...] -> [left-s1, right-s1, left-s2, right-s2, left-s3, right-s3, ...]
            val audioDataLentgh = audioData.size

            if (convertedSamples == null || convertedSamples!!.size < audioDataLentgh * 2) {
                convertedSamples = FloatArray(audioDataLentgh * 2)
            }

            for (i in 0 until audioDataLentgh) {
                val baseIndex = i * 2
                convertedSamples!![baseIndex + 1] = audioData[i]
                convertedSamples!![baseIndex] = convertedSamples!![baseIndex + 1]
            }

            track.write(convertedSamples!!, sizeInFloats = audioDataLength * 2)
        }
    }

    override fun release() {
        track.close()
    }

    class Factory(val sampleRate: Int = ReaStream.DEFAULT_SAMPLE_RATE) : AudioPacketHandler.Factory {
        override fun create(): AudioPacketHandler {
            return AudioTrackSink(sampleRate = sampleRate)
        }
    }
}

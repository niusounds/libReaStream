package com.eje_c.libreastream

class AudioTrackSink(sampleRate: Int) : AutoCloseable, ReaStreamReceiverService.OnReaStreamPacketListener {

    private val track = AudioTrack(sampleRate)
    private var convertedSamples: FloatArray? = null

    override fun close() {
        track.close()
    }

    override fun onReceive(packet: ReaStreamPacket) {
        if (packet.isAudioData) {

            val sizeInFloats = packet.blockLength / ReaStreamPacket.PER_SAMPLE_BYTES

            if (packet.channels.toInt() == 2) {

                // Interleave samples
                // [left-s1, left-s2, left-s3, ..., left-sN, right-s1, right-s2, right-s3, ..., right-sN]
                // -> [left-s1, right-s1, left-s2, right-s2, left-s3, right-s3, ..., left-sN, right-sN]

                if (convertedSamples == null || convertedSamples!!.size < sizeInFloats) {
                    convertedSamples = FloatArray(sizeInFloats)
                }

                packet.getInterleavedAudioData(convertedSamples!!)
                track.write(convertedSamples!!, sizeInFloats = sizeInFloats)

            } else if (packet.channels.toInt() == 1) {

                // Convert mono -> stereo
                // [s1, s2, s3, ...] -> [left-s1, right-s1, left-s2, right-s2, left-s3, right-s3, ...]
                val audioData = packet.audioData
                val audioDataLentgh = audioData!!.size

                if (convertedSamples == null || convertedSamples!!.size < audioDataLentgh * 2) {
                    convertedSamples = FloatArray(audioDataLentgh * 2)
                }

                for (i in 0 until audioDataLentgh) {
                    val baseIndex = i * 2
                    convertedSamples!![baseIndex + 1] = audioData[i]
                    convertedSamples!![baseIndex] = convertedSamples!![baseIndex + 1]
                }

                track.write(convertedSamples!!, sizeInFloats = sizeInFloats * 2)
            }
        }
    }
}

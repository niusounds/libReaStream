package com.eje_c.libreastream

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

class AudioTrackSink(sampleRate: Int) : AutoCloseable, ReaStreamReceiverService.OnReaStreamPacketListener {
    private val track: AudioTrack
    private var convertedSamples: FloatArray? = null

    init {
        val bufferSize = Math.max(
                AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT),
                ReaStreamPacket.MAX_BLOCK_LENGTH * FLOAT_BYTE_SIZE
        )
        track = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT, bufferSize, AudioTrack.MODE_STREAM)
    }

    /**
     * Must call this before first [.onReceive].
     */
    fun start() {

        if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
            track.play()
        }
    }

    fun stop() {

        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
            track.stop()
        }
    }

    override fun close() {
        track.release()
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
                track.write(convertedSamples!!, 0, sizeInFloats, AudioTrack.WRITE_NON_BLOCKING)

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

                track.write(convertedSamples!!, 0, sizeInFloats * 2, AudioTrack.WRITE_NON_BLOCKING)
            }
        }
    }

    companion object {
        private const val FLOAT_BYTE_SIZE = java.lang.Float.SIZE / java.lang.Byte.SIZE
    }
}

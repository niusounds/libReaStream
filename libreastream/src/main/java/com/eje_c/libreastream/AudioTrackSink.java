package com.eje_c.libreastream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioTrackSink implements AutoCloseable, ReaStreamReceiverService.OnReaStreamPacketListener {

    private static final int FLOAT_BYTE_SIZE = Float.SIZE / Byte.SIZE;
    private final AudioTrack track;
    private float[] convertedSamples;

    public AudioTrackSink(int sampleRate) {
        int bufferSize = Math.max(
                AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT),
                ReaStreamPacket.MAX_BLOCK_LENGTH * FLOAT_BYTE_SIZE
        );
        track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT, bufferSize, AudioTrack.MODE_STREAM);
    }

    /**
     * Must call this before first {@link #onReceive(ReaStreamPacket)}.
     */
    public void start() {

        if (track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            track.play();
        }
    }

    public void stop() {

        if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            track.stop();
        }
    }

    @Override
    public void close() {
        if (track != null) {
            track.release();
        }
    }

    @Override
    public void onReceive(ReaStreamPacket packet) {
        if (packet.isAudioData()) {

            final int sizeInFloats = packet.blockLength / ReaStreamPacket.PER_SAMPLE_BYTES;

            if (packet.channels == 2) {

                // Interleave samples
                // [left-s1, left-s2, left-s3, ..., left-sN, right-s1, right-s2, right-s3, ..., right-sN]
                // -> [left-s1, right-s1, left-s2, right-s2, left-s3, right-s3, ..., left-sN, right-sN]

                if (convertedSamples == null || convertedSamples.length < sizeInFloats) {
                    convertedSamples = new float[sizeInFloats];
                }

                packet.getInterleavedAudioData(convertedSamples);
                track.write(convertedSamples, 0, sizeInFloats, AudioTrack.WRITE_NON_BLOCKING);

            } else if (packet.channels == 1) {

                // Convert mono -> stereo
                // [s1, s2, s3, ...] -> [left-s1, right-s1, left-s2, right-s2, left-s3, right-s3, ...]
                final float[] audioData = packet.audioData;
                final int audioDataLentgh = audioData.length;

                if (convertedSamples == null || convertedSamples.length < audioDataLentgh * 2) {
                    convertedSamples = new float[audioDataLentgh * 2];
                }

                for (int i = 0; i < audioDataLentgh; i++) {
                    final int baseIndex = i * 2;
                    convertedSamples[baseIndex] = convertedSamples[baseIndex + 1] = audioData[i];
                }

                track.write(convertedSamples, 0, sizeInFloats * 2, AudioTrack.WRITE_NON_BLOCKING);
            }
        }
    }
}

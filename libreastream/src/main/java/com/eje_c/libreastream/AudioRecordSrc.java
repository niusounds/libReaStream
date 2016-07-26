package com.eje_c.libreastream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;

import java.nio.FloatBuffer;

public class AudioRecordSrc implements AutoCloseable {
    private static final float SHORT_TO_FLOAT = 1.0f / Short.MAX_VALUE;
    private final AudioRecord record;
    private final float[] recordBuffer;
    private final FloatBuffer floatBuffer;
    private short[] shortBuffer;

    public AudioRecordSrc(int sampleRate) {
        final int channel = AudioFormat.CHANNEL_IN_MONO;
        final int audioFormat = isAndroidM() ? AudioFormat.ENCODING_PCM_FLOAT : AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channel, audioFormat);
        record = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel, audioFormat, bufferSize);
        floatBuffer = FloatBuffer.allocate(bufferSize / 4);
        recordBuffer = floatBuffer.array();

        if (!isAndroidM()) {
            shortBuffer = new short[bufferSize / 4];
        }
    }


    /**
     * Must call this before first {@link #read()}.
     */
    public void start() {

        if (record.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
            record.startRecording();
        }
    }

    public void stop() {

        if (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            record.stop();
        }
    }

    @Override
    public void close() {
        if (record != null) {
            record.release();
        }
    }

    /**
     * Read audio data from {@link android.media.AudioTrack} and return it.
     *
     * @return Audio data FloatBuffer.
     */
    public FloatBuffer read() {

        if (isAndroidM()) {
            int readCount = record.read(recordBuffer, 0, recordBuffer.length, AudioRecord.READ_NON_BLOCKING);
            floatBuffer.limit(readCount);
        } else {
            int readCount = record.read(shortBuffer, 0, shortBuffer.length);

            // Convert short[] to float[]
            floatBuffer.position(0);

            for (int i = 0; i < readCount; i++) {
                float f = (float) shortBuffer[i] * SHORT_TO_FLOAT;
                floatBuffer.put(f);
            }

            floatBuffer.limit(readCount);
        }

        return floatBuffer;
    }

    private static boolean isAndroidM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}

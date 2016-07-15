package com.eje_c.reastream;

import android.annotation.SuppressLint;

import com.eje_c.libreastream.AudioRecordSrc;
import com.eje_c.libreastream.AudioTrackSink;
import com.eje_c.libreastream.ReaStreamPacket;
import com.eje_c.libreastream.ReaStreamReceiver;
import com.eje_c.libreastream.ReaStreamSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class ReaStream implements AutoCloseable {
    private boolean recording;
    private boolean playing;
    private int sampleRate = 44100;
    private ReaStreamSender sender;     // Non null while sending
    private ReaStreamReceiver receiver; // Non null while receiving
    private boolean enabled = true;
    private InetAddress remoteAddress;

    public void startSending() {

        if (!recording) {
            recording = true;
            new SenderThread().start();
        }
    }

    public void stopSending() {
        recording = false;
    }

    public void startReveiving() {

        if (!playing) {
            playing = true;
            new ReceiverThread().start();
        }
    }

    public void stopReceiving() {
        playing = false;
    }

    public boolean isSending() {
        return recording;
    }

    public boolean isReceiving() {
        return playing;
    }

    public void setIdentifier(String identifier) {

        if (sender != null) {
            sender.setIdentifier(identifier);
        }

        if (receiver != null) {
            receiver.setIdentifier(identifier);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setRemoteAddress(String remoteAddress) throws UnknownHostException {
        setRemoteAddress(InetAddress.getByName(remoteAddress));
    }

    public void setRemoteAddress(InetAddress remoteAddress) {
        this.remoteAddress = remoteAddress;

        if (sender != null) {
            sender.setRemoteAddress(remoteAddress);
        }
    }

    @Override
    public void close() {

        if (sender != null) {
            sender.close();
        }

        if (receiver != null) {
            receiver.close();
        }
    }

    private class SenderThread extends Thread {

        @SuppressLint("NewApi")
        @Override
        public void run() {

            try (ReaStreamSender sender = new ReaStreamSender();
                 AudioRecordSrc audioRecordSrc = new AudioRecordSrc(sampleRate)) {

                audioRecordSrc.start();
                ReaStream.this.sender = sender;

                sender.setSampleRate(sampleRate);
                sender.setChannels((byte) 1);
                sender.setRemoteAddress(remoteAddress);

                while (recording) {

                    // Read from mic and send it
                    FloatBuffer buffer = audioRecordSrc.read();
                    int readCount = buffer.limit();
                    if (enabled && readCount > 0) {
                        sender.send(buffer.array(), readCount);
                    }
                }

                audioRecordSrc.stop();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ReaStream.this.sender = null;
            }
        }
    }

    private class ReceiverThread extends Thread {

        @SuppressLint("NewApi")
        @Override
        public void run() {

            try (ReaStreamReceiver receiver = new ReaStreamReceiver();
                 AudioTrackSink audioTrackSink = new AudioTrackSink(sampleRate)) {
                audioTrackSink.start();
                ReaStream.this.receiver = receiver;

                while (playing) {
                    if (enabled) {
                        ReaStreamPacket packet = receiver.receive();
                        if (packet.isAudioData()) {
                            audioTrackSink.onReceive(packet);
                        } else if (packet.isMidiData()) {
                            // TODO
                            System.out.println(Arrays.toString(packet.midiEvents));
                        }
                    }
                }

                audioTrackSink.stop();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ReaStream.this.receiver = null;
            }
        }
    }
}

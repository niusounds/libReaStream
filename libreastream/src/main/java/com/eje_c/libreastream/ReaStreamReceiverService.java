package com.eje_c.libreastream;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReaStreamReceiverService extends Service {

    public class LocalBinder extends Binder {
        public ReaStreamReceiverService getService() {
            return ReaStreamReceiverService.this;
        }
    }

    public interface OnReaStreamPacketListener {
        void onReceive(ReaStreamPacket packet);
    }

    private final LocalBinder localBinder = new LocalBinder();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ReaStreamReceiver receiver; // Non null while receiving
    private boolean enabled = true;
    private OnReaStreamPacketListener onReaStreamPacketListener;
    private Future<?> future;
    private String identifier;
    private int timeout;

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public void setOnReaStreamPacketListener(OnReaStreamPacketListener onReaStreamPacketListener) {
        this.onReaStreamPacketListener = onReaStreamPacketListener;
    }

    public OnReaStreamPacketListener getOnReaStreamPacketListener() {
        return onReaStreamPacketListener;
    }

    public void startReveiving() {

        if (future == null) {
            future = executorService.submit(receiverTask);
        }
    }

    public void stopReceiving() {

        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    public boolean isReceiving() {
        return future != null;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;

        if (receiver != null) {
            receiver.setIdentifier(identifier);
        }
    }

    public void setTimeout(int timeout) throws SocketException {
        this.timeout = timeout;

        if (receiver != null) {
            receiver.setTimeout(timeout);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onDestroy() {

        stopReceiving();

        if (receiver != null) {
            receiver.close();
        }

        super.onDestroy();
    }

    private final Runnable receiverTask = new Runnable() {

        @SuppressLint("NewApi")
        @Override
        public void run() {

            try (ReaStreamReceiver receiver = new ReaStreamReceiver()) {
                ReaStreamReceiverService.this.receiver = receiver;

                receiver.setIdentifier(identifier);
                receiver.setTimeout(timeout);

                while (!Thread.interrupted()) {
                    if (enabled) {
                        ReaStreamPacket packet = receiver.receive();
                        if (onReaStreamPacketListener != null) {
                            onReaStreamPacketListener.onReceive(packet);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ReaStreamReceiverService.this.receiver = null;
            }
        }
    };
}

package com.eje_c.reastream.sample.receiverservice;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.eje_c.libreastream.AudioTrackSink;
import com.eje_c.libreastream.ReaStreamReceiverService;

public class MainActivity extends Activity {

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((ReaStreamReceiverService.LocalBinder) binder).getService();
            service.setOnReaStreamPacketListener(audioTrackSink);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private ReaStreamReceiverService service;
    private AudioTrackSink audioTrackSink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindService(new Intent(this, ReaStreamReceiverService.class), conn, BIND_AUTO_CREATE);

        audioTrackSink = new AudioTrackSink(44100);
        audioTrackSink.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(conn);

        audioTrackSink.close();
    }

    public void startReceiving(View view) {

        if (service != null) {
            service.startReveiving();
        }
    }

    public void stopReceiving(View view) {

        if (service != null) {
            service.stopReceiving();
        }
    }
}

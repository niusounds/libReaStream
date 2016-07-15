package com.eje_c.midisample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.eje_c.libreastream.MidiEvent;
import com.eje_c.libreastream.ReaStreamSender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

public class MainActivity extends Activity {
    private ReaStreamSender sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            sender = new ReaStreamSender();
        } catch (SocketException e) {
            throw new RuntimeException("Cannot create ReaStreamSender", e);
        }
    }

    @Override
    protected void onDestroy() {
        sender.close();
        super.onDestroy();
    }

    public void send(View view) {
        new Thread() {
            @Override
            public void run() {
                try {
                    sender.setRemoteAddress(InetAddress.getByName("192.168.10.37"));
                    sender.send(MidiEvent.create(MidiEvent.NOTE_ON, 0, 60, 100));

                    Thread.sleep(500);

                    sender.send(MidiEvent.create(MidiEvent.NOTE_OFF, 0, 60, 0));

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

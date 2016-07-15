# libReaStream

Android audio streaming library which can interact with [ReaStream](http://www.reaper.fm/reaplugs/).

## Launch sample

1. Launch REAPER.
2. Add track.
3. Add ReaStream to track.
4. Set ReaStream to *Receive audio/MIDI*.
5. Build project and run app.
6. Choose *Send audio* in app.
7. Enter REAPER machine's IP address in *Remote address* text field.
8. Now, start streaming from Android to REAPER!

## Receiving audio

Receive ReaStream packet and send it to speaker.

```java
public class MyActivity extends Activity {

    private ServiceConnection conn = new ServiceConnection() {
        AudioTrackSink audioTrackSink;

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            // Get service instance
            ReaStreamReceiverService service = ((ReaStreamReceiverService.LocalBinder) iBinder).getService();

            int sampleRate = 44100;

            // Create speaker output
            audioTrackSink = new AudioTrackSink(sampleRate);
            audioTrackSink.start();

            service.startReveiving();

            // connect ReaStreamReceiverService to AudioTrackSink
            service.setOnReaStreamPacketListener(audioTrackSink);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // Release resources
            audioTrackSink.close();
            audioTrackSink = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindService(new Intent(this, ReaStreamReceiverService.class), conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }
}
```

## Sending audio

Get audio samples from mic and send it to remote address.

```java
int sampleRate = 44100;
String remoteAddress = "192.168.1.5";

try (AudioRecordSrc audioRecordSrc = new AudioRecordSrc(sampleRate);
     ReaStreamSender reaStreamSender = new ReaStreamSender()) {

    reaStreamSender.setSampleRate(sampleRate);
    audioRecordSrc.start();

    reaStreamSender.setRemoteAddress(InetAddress.getByName(remoteAddress));

    while (true) {

        // Read audio from mic
        FloatBuffer floatBuffer = audioRecordSrc.read();

        // Send it
        reaStreamSender.send(floatBuffer.array(), floatBuffer.limit());
    }

} catch (IOException e) {
    e.printStackTrace();
}
```

Note: Above codes must be run in background thread. Don't do it in UI-thread.
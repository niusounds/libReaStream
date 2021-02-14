# libReaStream

Android audio streaming library which can interact with [ReaStream](http://www.reaper.fm/reaplugs/).

## How to use example app

1. Launch REAPER.
2. Add track in REAPER.
3. Add ReaStream to track.
4. Build *app* module and launch it in Android.

and...

| | Stream from REAPER to Android | Stream from Android to REAPER |
| --- | --- | --- |
| 5. | Set REAPER's ReaStream to *Send audio/MIDI*. | Set REAPER's ReaStream to *Receive audio/MIDI*. |
| 6. | In Android app, choose *Receive audio*. | In Android app, choose *Send audio*. |
| 7. | Enter Android device's IP address in REAPER's text field | Enter REAPER machine's IP address in Android's text field.

## How to use libreastream in your app

```gradle
// app/build.gradle

repositories {
    google()
    maven { url 'https://jitpack.io' }
}


dependencies {
    implementation 'com.github.niusounds:libReaStream:0.0.2' // Add this
}
```

See sample app code for more information.

### Receive audio from remote

For receiving audio and stream it to speaker, simply call `ReaStream.startReceiving()`:

```kotlin
val reaStream = ReaStream()
reaStream.startReceiving()

// After done
reaStream.stopReceiving()
```

Received audio will be routed to speaker output.

If you want to process audio packet rather than output to speaker, create class which implements `AudioPacketHandler` and `AudioPacketHandler.Factory`.

```kotlin
class MyAudioPacketHandler : AudioPacketHandler {
    override fun process(channels: Int, sampleRate: Int, audioData: FloatArray, audioDataLength: Int) {
        // This is called in background thread. Don't update UI directly here.
        //
        // audioData is filled from index 0 to audioDataLength - 1.
        // Sample order is (N = audioDataLength / channels)
        // [ch1-s1, ch1-s2, ch1-s3, ..., ch1-sN, ch2-s1, ch2-s2, ch2-s3, ..., ch2-sN]
        // 
        // Typically channels will be 2 or 1. But ReaStream VST plugin supports up to 8 channels.
    }

    override fun release() {
        // Do something on ReaStream.close() is called.
    }
}

class MyAudioPacketHandlerFactory : AudioPacketHandler.Factory {
    override fun create(): AudioPacketHandler = MyAudioPacketHandler()
}
```

Then pass `MyAudioPacketHandlerFactory` to `ReaStream`'s constructor.

```kotlin
val reaStream = ReaStream(audioPacketHandlerFactory = MyAudioPacketHandlerFactory())
```

### Send audio to remote

Use `ReaStream.startSending()` to send Android's microphone to remote.

```kotlin
// Specify your target machine's IP address.
// If ommitted, default send target is broadcast address.
reaStream.remoteAddress = InetAddress.getByName("192.168.10.10")
reaStream.startSending()

// After done
reaStream.stopSending()
```

If you stream other audio source, create class which implements `AudioStreamSource` and `AudioStreamSourceFactory`.

```kotlin
class MyAudioStreamSource : AudioStreamSource {
    override read(): FloatBuffer {
        // Return audio data.
        // FloatBuffer must be filled with audio data starting at position 0 and set valid audio data length to [FloatBuffer.limit].
    }

    override fun release() {
        // Do something on ReaStream.close() is called.
    }
}

class MyAudioStreamSourceFactory : AudioStreamSource.Factory {
    override fun create(): AudioStreamSource = MyAudioStreamSource()
}
```

Then pass `MyAudioStreamSourceFactory` to `ReaStream`'s constructor.

```kotlin
val reaStream = ReaStream(audioStreamSourceFactory = MyAudioStreamSourceFactory())
```

### Receive MIDI event from remote

If you want to handle MIDI event, create class which implements `MidiPacketHandler` and `MidiPacketHandler.Factory`.

```kotlin
class MyMidiPacketHandler : MidiPacketHandler {
    override fun process(midiEvent: MidiEvent) {
        // This is called in background thread. Don't update UI directly here.
    }

    override fun release() {
        // Do something on ReaStream.close() is called.
    }
}

class MyMidiPacketHandlerFactory : MidiPacketHandler.Factory {
    override fun create(): MidiPacketHandler = MyMidiPacketHandler()
}
```

Then pass `MyMidiPacketHandlerFactory` to `ReaStream`'s constructor.

```kotlin
reaStream = ReaStream(midiPacketHandlerFactory = MidiHandlerFactory())
```

### Send MIDI event to remote

Currently, `ReaStream` does not support sending MIDI event. Instead, `ReaStreamSender` can be used to send MIDI event.

```kotlin
val sender = ReaStreamSender(remote = InetSocketAddress("192.168.1.2", ReaStream.DEFAULT_PORT))

// Send note on
sender.send(MidiEvent.create(MidiEvent.NOTE_ON, 0, noteNumber, 100))
// Send note off
sender.send(MidiEvent.create(MidiEvent.NOTE_OFF, 0, noteNumber, 0))

// After use
sender.close()
```

See *midisample* example app.


### Release resources

You must call `ReaStream.close()` if ReaStream is no more needed.

```kotlin
reaStream.close()
```

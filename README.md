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

## How to use

`ReaStream` class is most simple entry point. For receiving audio and stream it to speaker, simply call `ReaStream.startReceiving()`:

```kotlin
val reaStream = ReaStream()
reaStream.startReceiving()

// After done
reaStream.stopReceiving()
```

If you want to handle MIDI event, use `ReaStream.onMidiEvents`.

```kotlin
reaStream.onMidiEvents = { midiEvents: Array<MidiEvent> ->

    // This callback is called in background thread.
    // UI update must be in main thread.

    runOnUiThread {
        // Update UI etc...
    }
}
```

If you want to send audio from Android to remote, use `ReaStream.startSending()`.

```kotlin
reaStream.remoteAddress= InetAddress.getByName("192.168.10.10")
reaStream.startSending()

// After done
reaStream.stopSending()
```

You must call `ReaStream.close()` if ReaStream is no more needed.

```kotlin
reaStream.close()
```

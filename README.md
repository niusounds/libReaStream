[![](https://jitpack.io/v/niusounds/libReaStream.svg)](https://jitpack.io/#niusounds/libReaStream)

# libReaStream

Android audio streaming library which can interact with [ReaStream](http://www.reaper.fm/reaplugs/).

## Receive audio/MIDI from REAPER

```kotlin
val audioData = FloatArray(ReaStreamPacket.MAX_BLOCK_LENGTH * 2) // Make an array for audio of sufficient size

val packets = receiveReaStream()
packets.collect { packet ->
    if (packet.isAudio) {
        val audioDataLength = packet.readAudio(audioData)
        // audioData[0 until audioDataLength] is filled.
        // handle audio samples
    } else if (packet.isMidi) {
        packet.midiEvents.forEach { midiEvent ->
            // handle MIDI event
        }
    }
}
```

Simply use `AudioTrackOutput` to play received audio.

```kotlin
receiveReaStream().play(sampleRate = 48000) // sampleRate must be equal to input audio
```

If you want to play audio and process MIDI at the same time, use `SharedFlow`.

```kotlin
coroutineScope {
    val packets = receiveReaStream().shareIn(this, SharingStarted.WhileSubscribed())
    launch {
        packets.play(sampleRate = 48000)
    }
    launch {
        packets.filter { it.isMidi }
            .collect { packet ->
                packet.midiEvents.forEach { midiEvent ->
                    // handle MIDI event
                }
            }
    }
}
```


## Send audio/MIDI to REAPER

```kotlin
val sampleRate = 48000 // Usually 44100 or 48000 is good choice.
val channels = 2       // Only 1 or 2 are currently supported.
val sender = ReaStreamSender(
    identifier = "default",      // Must be the same as REAPER
    sampleRate = sampleRate,     // Input audio sample rate.
    channels = channels,         // Input audio channels.
    remoteHost = "192.168.10.2", // IP address of Mac/PC which is running REAPER
)

// send MIDI
val data = byteArrayOf(0x90.toByte(), 60, 127)
sender.send(data)

// or something like
sender.send(midiData(MidiCommand.NoteOn, channel, noteNumber, velocity))
sender.send(midiData(MidiCommand.NoteOff, channel, noteNumber, velocity))

// send audio
val input = AudioRecordInput(
    sampleRate = sampleRate,
    channels = channels,
)
input.readAudio()
    // ReaStream uses non-interleaved arrangement.
    // De-interleaving is required if channels is more than 1.
    .map { it.deInterleave(channels = channels) }
    .collect { audioData ->
        sender.send(audioData)
    }
```


## How to use example app

1. Launch REAPER.
2. Add track in REAPER.
3. Add ReaStream to track.
4. Build *app* module and launch it in Android.

and...

|    | Stream from REAPER to Android                            | Stream from Android to REAPER                              |
|----|----------------------------------------------------------|------------------------------------------------------------|
| 5. | Set REAPER's ReaStream to *Send audio/MIDI*.             | Set REAPER's ReaStream to *Receive audio/MIDI*.            |
| 6. | In Android app, choose *Receive audio*.                  | In Android app, choose *Send audio*.                       |
| 7. | Enter Android device's IP address in REAPER's text field | Enter REAPER machine's IP address in Android's text field. |

## How to use libreastream in your app

```gradle
// app/build.gradle

repositories {
    google()
    maven { url 'https://jitpack.io' } // Add this
}


dependencies {
    implementation 'com.github.niusounds:libReaStream:0.2.0' // Add this
}
```

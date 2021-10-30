package com.niusounds.libreastream.midisample

import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.niusounds.libreastream.MidiCommand
import com.niusounds.libreastream.midiData
import com.niusounds.libreastream.midisample.databinding.ActivityMainBinding
import com.niusounds.libreastream.sender.ReaStreamSender
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val sender = ReaStreamSender(
        identifier = "default",
        sampleRate = 48000, // This sample is MIDI only but sampleRate argument is required currently.
        channels = 1,
        remoteHost = "192.168.86.155", // TODO change here
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)

            keyC.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 60) }
            keyCis.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 61) }
            keyD.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 62) }
            keyDis.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 63) }
            keyE.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 64) }
            keyF.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 65) }
            keyFis.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 66) }
            keyG.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 67) }
            keyGis.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 68) }
            keyA.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 69) }
            keyAis.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 70) }
            keyB.setOnTouchListener { _, motionEvent -> noteEvent(motionEvent, 71) }
        }
    }

    override fun onDestroy() {
        sender.close()
        super.onDestroy()
    }

    private fun noteEvent(motionEvent: MotionEvent, noteNumber: Int): Boolean {

        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lifecycleScope.launch {
                    sender.send(midiData(MidiCommand.NoteOn, 0, noteNumber, 100))
                }
            }

            MotionEvent.ACTION_UP -> {
                lifecycleScope.launch {
                    sender.send(midiData(MidiCommand.NoteOff, 0, noteNumber, 0))
                }
            }
        }

        // Do not override default behavior
        return false
    }
}

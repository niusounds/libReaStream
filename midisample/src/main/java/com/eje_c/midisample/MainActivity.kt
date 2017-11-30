package com.eje_c.midisample

import android.app.Activity
import android.os.Bundle
import android.view.MotionEvent
import com.eje_c.libreastream.MidiEvent
import com.eje_c.libreastream.ReaStreamSender
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val sender = ReaStreamSender()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    override fun onDestroy() {
        executor.shutdownNow()
        sender.close()
        super.onDestroy()
    }

    fun noteEvent(motionEvent: MotionEvent, noteNumber: Int): Boolean {

        when (motionEvent.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                executor.submit {
                    sender.send(MidiEvent.create(MidiEvent.NOTE_ON, 0, noteNumber, 100))
                }
            }

            MotionEvent.ACTION_UP -> {
                executor.submit {
                    sender.send(MidiEvent.create(MidiEvent.NOTE_OFF, 0, noteNumber, 0))
                }
            }
        }

        // Do not override default behavior
        return false
    }
}

package com.niusounds.flowsample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.niusounds.libreastream.flow.AudioTrackOutput
import com.niusounds.libreastream.flow.ReaStreamReceiver
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val text: TextView = findViewById(R.id.text)
        lifecycleScope.launch {
            val packets = ReaStreamReceiver().packets
            launch {
                AudioTrackOutput().play(packets)
            }
            launch {
                packets.filter { it.isMidi }
                    .collect { packet ->
                        packet.midiEvents.forEach { midiEvent ->
                            text.text = midiEvent.toString()
                        }
                    }
            }
        }
    }
}
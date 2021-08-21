package com.niusounds.flowsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.niusounds.flowsample.databinding.ActivityMainBinding
import com.niusounds.libreastream.receiver.AudioTrackOutput
import com.niusounds.libreastream.receiver.receiveReaStream
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            lifecycleScope.launch {
                val packets = receiveReaStream().shareIn(this, SharingStarted.WhileSubscribed())
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
}
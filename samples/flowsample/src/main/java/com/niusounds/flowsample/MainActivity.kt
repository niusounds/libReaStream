package com.niusounds.flowsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.niusounds.flowsample.databinding.ActivityMainBinding
import com.niusounds.libreastream.receiver.AudioTrackOutput
import com.niusounds.libreastream.receiver.receiveReaStream
import java.net.Inet4Address
import java.net.NetworkInterface
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

            // Show instruction
            findMyIpAddress()?.let { myIp -> text.text = getString(R.string.message, myIp) }

            lifecycleScope.launch {
                val packets = receiveReaStream().shareIn(this, SharingStarted.WhileSubscribed())

                // Play received audio
                launch {
                    AudioTrackOutput().play(packets)
                }

                // Show received MIDI message
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

    private fun findMyIpAddress(): String? {
        NetworkInterface.getNetworkInterfaces().asSequence().forEach { intf ->
            intf.inetAddresses.asSequence().forEach { inetAddress ->
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.hostAddress
                }
            }
        }

        return null
    }
}
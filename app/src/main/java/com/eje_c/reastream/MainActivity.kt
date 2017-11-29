package com.eje_c.reastream

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import kotlinx.android.synthetic.main.activity_main.*
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var reaStream: ReaStream
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init lateinit fields
        reaStream = ReaStream()
        prefs = getSharedPreferences("app_status", Context.MODE_PRIVATE)

        // Watch identifier text field
        identifier.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val newVal = s.toString()

                // Save identifier value to SharedPreference
                prefs.edit().putString("identifier", newVal).apply()

                // Set identifier
                reaStream.setIdentifier(newVal)
            }
        })

        // Set previously saved identifier
        val defaultIdentifier = prefs.getString("identifier", null)
        if (defaultIdentifier != null) {
            identifier.setText(defaultIdentifier)
            reaStream.setIdentifier(defaultIdentifier)
        }

        // Switch ReaStream mode
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_mode_receive -> {
                    if (reaStream.isSending) {
                        reaStream.stopSending()
                    }
                    if (!reaStream.isReceiving) {
                        reaStream.startReveiving()
                    }
                }
                R.id.radio_mode_send -> {
                    if (reaStream.isReceiving) {
                        reaStream.stopReceiving()
                    }
                    if (!reaStream.isSending) {
                        reaStream.startSending()
                    }
                }
            }
        }

        // Bind "enabled" checkbox to ReaStream.isEnabled
        enabled.isChecked = reaStream.isEnabled
        enabled.setOnCheckedChangeListener { _, isChecked -> reaStream.isEnabled = isChecked }

        // Default is receiving mode
        reaStream.startReveiving()

        // Watch remote address text field
        remoteAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val newVal = s.toString()

                // Check IP address format
                if (newVal.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}".toRegex())) {

                    try {
                        reaStream.setRemoteAddress(newVal)

                        // Save
                        prefs.edit().putString("remoteAddress", newVal).apply()
                    } catch (e: UnknownHostException) {
                        e.printStackTrace()
                    }
                }
            }
        })

        // Set previously saved remote address
        val defaultRemoteAddress = prefs.getString("remoteAddress", null)
        if (defaultRemoteAddress != null) {
            remoteAddress.setText(defaultRemoteAddress)

            try {
                reaStream.setRemoteAddress(defaultRemoteAddress)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            }
        }

        // Show MIDI events
        reaStream.onMidiEvents = { midiEvents ->

            // This callback is called in background thread.
            // UI update must be in main thread.

            runOnUiThread {
                this.midiEvents.text = midiEvents.map { ev -> ev.toString() }.joinToString()
            }
        }
    }

    override fun onDestroy() {
        reaStream.close()
        super.onDestroy()
    }
}

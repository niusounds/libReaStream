package com.eje_c.reastream

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import com.eje_c.libreastream.ReaStream
import kotlinx.android.synthetic.main.activity_main.*
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private var reaStream: ReaStream = ReaStream()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init lateinit fields
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
                updateReaStream(identifier = newVal)
            }
        })

        // Set previously saved identifier
        val defaultIdentifier = prefs.getString("identifier", null)
        if (defaultIdentifier != null) {
            identifier.setText(defaultIdentifier)
            updateReaStream(identifier = defaultIdentifier)
        }

        // Switch ReaStream mode
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_mode_receive -> {
                    reaStream.stopSending()
                    reaStream.startReceiving()
                }
                R.id.radio_mode_send -> {
                    reaStream.stopReceiving()
                    reaStream.startSending()
                }
            }
        }

        // Bind "enabled" checkbox to ReaStream.isEnabled
        enabled.isChecked = reaStream.isEnabled
        enabled.setOnCheckedChangeListener { _, isChecked -> reaStream.isEnabled = isChecked }

        // Default is receiving mode
        reaStream.startReceiving()

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

    private fun updateReaStream(identifier: String = reaStream.identifier) {

        // Original state
        val receiving = reaStream.isReceiving
        val sending = reaStream.isSending
        val remoteAddress = reaStream.remoteAddress

        // Close current session
        reaStream.close()

        // Create new instance
        reaStream = ReaStream(identifier = identifier)

        // Restore previous state
        reaStream.remoteAddress = remoteAddress

        if (receiving) {
            reaStream.startReceiving()
        }

        if (sending) {
            reaStream.startSending()
        }
    }

    override fun onDestroy() {
        reaStream.close()
        super.onDestroy()
    }
}

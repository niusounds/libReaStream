package com.niusounds.libreastream.example

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import com.niusounds.libreastream.MidiEvent
import com.niusounds.libreastream.MidiPacketHandler
import com.niusounds.libreastream.ReaStream
import kotlinx.android.synthetic.main.activity_main.*
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 111
    }

    private var reaStream: ReaStream? = null
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("app_status", Context.MODE_PRIVATE)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            initReaStream()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initReaStream()
                } else {
                    finish()
                }
            }
        }
    }

    private fun initReaStream() {
        // Create ReaStream instance
        reaStream = ReaStream(
                midiPacketHandlerFactory = MidiHandlerFactory()
        )

        // Watch identifier text field
        identifier.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val newVal = s.toString()

                // Save identifier value to SharedPreference
                prefs.edit().putString("identifier", newVal).apply()

                // Set identifier
                reaStream!!.identifier = newVal
            }
        })

        // Set previously saved identifier
        val defaultIdentifier = prefs.getString("identifier", null)
        if (defaultIdentifier != null) {
            identifier.setText(defaultIdentifier)
            reaStream!!.identifier = defaultIdentifier
        }

        // Switch ReaStream mode
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_mode_receive -> {
                    reaStream!!.stopSending()
                    reaStream!!.startReceiving()
                }
                R.id.radio_mode_send -> {
                    reaStream!!.stopReceiving()
                    reaStream!!.startSending()
                }
            }
        }

        // Bind "enabled" checkbox to ReaStream.isEnabled
        enabled.isChecked = reaStream!!.isEnabled
        enabled.setOnCheckedChangeListener { _, isChecked -> reaStream!!.isEnabled = isChecked }

        // Default is receiving mode
        reaStream!!.startReceiving()

        // Watch remote address text field
        remoteAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                val newVal = s.toString()

                // Check IP address format
                if (newVal.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}".toRegex())) {

                    try {
                        reaStream!!.setRemoteAddress(newVal)

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
                reaStream!!.setRemoteAddress(defaultRemoteAddress)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        // Release ReaStream when Activity is destroyed
        reaStream?.close()
        super.onDestroy()
    }

    /**
     * Show MIDI event in text field.
     */
    inner class MidiHandler : MidiPacketHandler {
        override fun process(midiEvent: MidiEvent) {
            runOnUiThread {
                this@MainActivity.midiEvents.text = midiEvent.toString()
            }
        }

        override fun release() {
            // Do nothing
        }
    }

    inner class MidiHandlerFactory : MidiPacketHandler.Factory {
        override fun create(): MidiPacketHandler = MidiHandler()
    }
}

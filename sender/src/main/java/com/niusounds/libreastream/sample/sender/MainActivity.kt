package com.niusounds.libreastream.sample.sender

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.niusounds.libreastream.sender.AudioRecordInput
import com.niusounds.libreastream.sender.ReaStreamSender
import com.niusounds.libreastream.sender.deInterleave
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val checkPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecording(
                sampleRate = 48000,
                channels = 2,
            )
        } else {
            Toast.makeText(
                applicationContext,
                "Permission is not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val recording = MutableStateFlow(false)
    private val remoteHost = MutableStateFlow("192.168.86.79")
    private var recordingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sending by recording.collectAsState()
            val host by remoteHost.collectAsState()
            MaterialTheme {
                SenderScreen(
                    remoteHost = host,
                    sending = sending,
                    onChangeRemoteHost = { remoteHost.value = it },
                    onStart = {
                        checkPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onStop = {
                        stopRecording()
                    },
                    onSendMidi = {
                        sendMidi(it)
                    }
                )
            }
        }
    }

    private fun startRecording(sampleRate: Int, channels: Int) {
        check(!recording.value) { "startRecording() is called twice" }
        recording.value = true

        recordingJob = lifecycleScope.launchWhenStarted {
            val input = AudioRecordInput(
                sampleRate = sampleRate,
                channels = channels,
            )
            val sender = ReaStreamSender(
                identifier = "android",
                sampleRate = sampleRate,
                channels = channels,
                remoteHost = remoteHost.value,
            )

            input.readAudio()
                // ReaStream uses non-interleaved arrangement
                .map { it.deInterleave(channels = channels) }
                .collect { audioData ->
                    try {
                        sender.send(audioData)
                    } catch (e: Exception) {
                        Log.e("Sender", "Error: $e")
                        stopRecording()
                        Toast.makeText(
                            applicationContext,
                            "Stopped because error $e",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    private fun stopRecording() {
        recording.value = false
        recordingJob?.cancel()
        recordingJob = null
    }

    private fun sendMidi(data: ByteArray) {
        val sender = ReaStreamSender(
            identifier = "android",
            sampleRate = 0,
            channels = 0,
            remoteHost = remoteHost.value,
        )
         lifecycleScope.launch {
            sender.send(data)
        }
    }
}

@Composable
fun SenderScreen(
    remoteHost: String,
    sending: Boolean,
    onChangeRemoteHost: (String) -> Unit,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
    onSendMidi: (ByteArray) -> Unit = {},
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = remoteHost,
                onValueChange = onChangeRemoteHost,
                label = { Text("Remote host") }
            )
            Button(
                onClick = {
                    if (sending) {
                        onStop()
                    } else {
                        onStart()
                    }
                },
            ) {
                if (sending) {
                    Text("Stop sending")
                } else {
                    Text("Start sending")

                }
            }

            val coroutineScope = rememberCoroutineScope()
            Button(
                onClick = {
                    coroutineScope.launch {
                        onSendMidi(byteArrayOf(0x90.toByte(), 60, 127))
                        delay(1000)
                        onSendMidi(byteArrayOf(0x80.toByte(), 60, 0))
                    }
                },
            ) {
                Text("Send MIDI event")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    var sending by remember {
        mutableStateOf(false)
    }

    var host by remember {
        mutableStateOf("")
    }

    MaterialTheme {
        SenderScreen(
            remoteHost = host,
            sending = sending,
            onChangeRemoteHost = { host = it },
            onStart = { sending = true },
            onStop = { sending = false },
        )
    }
}
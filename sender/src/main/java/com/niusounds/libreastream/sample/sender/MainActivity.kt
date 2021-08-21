package com.niusounds.libreastream.sample.sender

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.niusounds.libreastream.sender.AudioRecordInput
import com.niusounds.libreastream.sender.KtorUdpSender
import com.niusounds.libreastream.sender.ReaStreamSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val checkPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startRecording()
        } else {
            Toast.makeText(
                applicationContext,
                "Permission is not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val recording = MutableStateFlow(false)
    private var recordingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sending by recording.collectAsState()
            MaterialTheme {
                SenderScreen(
                    sending = sending,
                    onStart = {
                        checkPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onStop = {
                        stopRecording()
                    }
                )
            }
        }
    }

    private fun startRecording() {
        check(!recording.value) { "startRecording() is called twice" }
        recording.value = true

        recordingJob = lifecycleScope.launchWhenStarted {
            withContext(Dispatchers.IO) {
                val input = AudioRecordInput(
                    sampleRate = 48000,
                    channels = 1,
                )
                val sender = ReaStreamSender(
                    identifier = "android",
                    sampleRate = 48000,
                    channels = 1,
                    sender = KtorUdpSender(
                        host = "192.168.86.79"
                    )
                )

                input.readAudio().collect { audioData ->
                    sender.send(audioData)
                }
            }
        }
    }

    private fun stopRecording() {
        recording.value = false
        recordingJob?.cancel()
        recordingJob = null
    }
}

@Composable
fun SenderScreen(
    sending: Boolean,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize()) {
        Button(
            modifier = Modifier.align(Alignment.Center),
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    var sending by remember {
        mutableStateOf(false)
    }
    MaterialTheme {
        SenderScreen(
            sending = sending,
            onStart = { sending = true },
            onStop = { sending = false }
        )
    }
}
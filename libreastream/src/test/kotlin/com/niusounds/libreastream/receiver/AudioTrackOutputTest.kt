package com.niusounds.libreastream.receiver

import android.media.AudioTrack
import com.niusounds.libreastream.sender.AudioDataSerializer
import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class AudioTrackOutputTest {
    @Before
    fun setup() {
        mockkStatic(AudioTrack::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun MockKMatcherScope.partial(expected: FloatArray, range: IntRange) =
        match<FloatArray> {
            range.all { i -> it[i] == expected[i] }
        }

    @Test
    fun testPlay() {
        val mockFactory = mockk<AudioTrackFactory>()
        val mockAudioTrack = mockk<AudioTrack>(relaxed = true)
        every { AudioTrack.getMinBufferSize(any(), any(), any()) } returns 42
        every { mockFactory.create(any(), any(), any()) } returns mockAudioTrack

        val audioTrackOutput = AudioTrackOutput(
            sampleRate = 44100,
            channels = 1,
            bufferScaleFactor = 1,
            audioTrackFactory = mockFactory,
        )

        val serializer = AudioDataSerializer(
            identifier = "test",
            sampleRate = 44100,
            channels = 1,
        )
        val testAudioData = FloatArray(100) { Random.nextFloat() }
        val testReaStreamPacket = ByteBufferReaStreamPacket(serializer.toByteBuffer(testAudioData))
        val testPacketFlow = flowOf(testReaStreamPacket)

        runBlocking {
            audioTrackOutput.play(testPacketFlow)
            verify { mockAudioTrack.play() }
            verify {
                mockAudioTrack.write(
                    partial(testAudioData, testAudioData.indices),
                    0,
                    testAudioData.size,
                    any()
                )
            }
            verify { mockAudioTrack.release() }
        }
    }
}
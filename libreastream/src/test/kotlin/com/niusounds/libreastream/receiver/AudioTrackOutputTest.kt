package com.niusounds.libreastream.receiver

import android.media.AudioTrack
import com.niusounds.libreastream.sender.AudioDataSerializer
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class AudioTrackOutputTest {
    private lateinit var mockFactory: (sampleRate: Int, channelMask: Int, bufferSize: Int) -> AudioTrack
    private lateinit var mockAudioTrack: AudioTrack

    @Before
    fun setup() {
        mockFactory = mockk()
        mockAudioTrack = mockk(relaxed = true)
        every { mockFactory(any(), any(), any()) } returns mockAudioTrack

        mockkStatic(AudioTrack::class)
        every { AudioTrack.getMinBufferSize(any(), any(), any()) } returns 42
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun MockKMatcherScope.partial(expected: FloatArray, range: IntRange) =
        match<FloatArray> {
            range.all { i -> it[i] == expected[i] }
        }

    private fun FloatArray.asReaStreamPacket(
        sampleRate: Int = 44100,
        channels: Int = 1,
    ): ReaStreamPacket =
        ByteBufferReaStreamPacket(
            AudioDataSerializer(
                identifier = "test",
                sampleRate = sampleRate,
                channels = channels,
            ).toByteBuffer(this)
        )

    @Test
    fun testMonoToMono() {
        val testAudioData = FloatArray(100) { Random.nextFloat() }
        val testPacketFlow = flowOf(testAudioData.asReaStreamPacket())

        runBlocking {
            testPacketFlow.play(
                sampleRate = 44100,
                channels = 1,
                bufferScaleFactor = 1,
                audioTrackFactory = mockFactory,
            )
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

    @Test
    fun testStereoToStereo() {
        // ReaStream packet stereo audio (non-interleaved)
        val testAudioData = floatArrayOf(
            // L
            0.1f, 0.2f, 0.3f,
            // R
            0.4f, 0.5f, 0.6f,
        )

        // written stereo audio (interleaved)
        val expectedAudioData = floatArrayOf(
            // L, R
            0.1f, 0.4f,
            0.2f, 0.5f,
            0.3f, 0.6f,
        )
        val testPacketFlow = flowOf(testAudioData.asReaStreamPacket(channels = 2))

        runBlocking {
            testPacketFlow.play(
                sampleRate = 44100,
                channels = 2,
                bufferScaleFactor = 1,
                audioTrackFactory = mockFactory,
            )
            verify { mockAudioTrack.play() }
            verify {
                mockAudioTrack.write(
                    partial(expectedAudioData, expectedAudioData.indices),
                    0,
                    expectedAudioData.size,
                    any()
                )
            }
            verify { mockAudioTrack.release() }
        }
    }

    @Test
    fun testMonoToStereo() {
        // input mono audio
        val testAudioData = FloatArray(100) { Random.nextFloat() }
        // mono -> stereo converted
        val expectedAudioData = testAudioData.flatMap { listOf(it, it) }.toFloatArray()

        val testPacketFlow = flowOf(testAudioData.asReaStreamPacket())

        runBlocking {
            testPacketFlow.play(
                sampleRate = 44100,
                channels = 2,
                bufferScaleFactor = 1,
                audioTrackFactory = mockFactory,
            )
            verify { mockAudioTrack.play() }
            verify {
                mockAudioTrack.write(
                    partial(expectedAudioData, expectedAudioData.indices),
                    0,
                    expectedAudioData.size,
                    any()
                )
            }
            verify { mockAudioTrack.release() }
        }
    }

    @Test
    fun testPlayStereoToMono() {
        // ReaStream packet stereo audio (non-interleaved)
        val testAudioData = floatArrayOf(
            // L
            0.1f, 0.2f, 0.3f,
            // R
            0.4f, 0.5f, 0.6f,
        )

        // written stereo audio (interleaved)
        val expectedAudioData = floatArrayOf(
            // (L + R) / 2
            (0.1f + 0.4f) * 0.5f,
            (0.2f + 0.5f) * 0.5f,
            (0.3f + 0.6f) * 0.5f,
        )
        val testPacketFlow = flowOf(testAudioData.asReaStreamPacket(channels = 2))

        runBlocking {
            testPacketFlow.play(
                sampleRate = 44100,
                channels = 1,
                bufferScaleFactor = 1,
                audioTrackFactory = mockFactory,
            )
            verify { mockAudioTrack.play() }
            verify {
                mockAudioTrack.write(
                    partial(expectedAudioData, expectedAudioData.indices),
                    0,
                    expectedAudioData.size,
                    any()
                )
            }
            verify { mockAudioTrack.release() }
        }
    }
}
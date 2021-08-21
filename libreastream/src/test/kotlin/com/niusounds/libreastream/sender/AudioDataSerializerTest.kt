package com.niusounds.libreastream.sender

import com.niusounds.libreastream.receiver.ByteBufferReaStreamPacket
import org.junit.Assert
import org.junit.Test

class AudioDataSerializerTest {
    /**
     * This test depends on ByteBufferReaStreamPacketTest.
     * If ByteBufferReaStreamPacketTest failed, this result is unreliable.
     */
    @Test
    fun testSerialize() {
        val serializer = AudioDataSerializer(
            identifier = "test",
            sampleRate = 44100,
            channels = 2,
        )

        val data = floatArrayOf(
            0.42f,
            0.11f,
            -0.95f,
            0.0f,
            -1.0f,
            -0.5f,
        )

        val buffer = serializer.toByteBuffer(data)

        val packet = ByteBufferReaStreamPacket(buffer)
        Assert.assertEquals(true, packet.isAudio)
        Assert.assertEquals("test", packet.identifier)
        Assert.assertEquals(44100, packet.sampleRate)
        Assert.assertEquals(2.toByte(), packet.channels)

        val readSamples = FloatArray(packet.blockLength / Float.SIZE_BYTES)
        packet.readAudio(readSamples)

        Assert.assertArrayEquals(data, readSamples, 0.00001f)
    }
}
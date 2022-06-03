package com.niusounds.libreastream.sender

import com.niusounds.libreastream.receiver.ByteBufferReaStreamPacket
import com.niusounds.libreastream.receiver.MidiEvent
import org.junit.Assert
import org.junit.Test

class MidiDataSerializerTest {
    /**
     * This test depends on ByteBufferReaStreamPacketTest.
     * If ByteBufferReaStreamPacketTest failed, this result is unreliable.
     */
    @Test
    fun testSerialize() {
        val serializer = MidiDataSerializer(
            identifier = "test",
        )

        val data = byteArrayOf(
            0x70.toByte(),
            60,
            127,
        )

        val buffer = serializer.toByteBuffer(midiData = data)

        val packet = ByteBufferReaStreamPacket(buffer)
        Assert.assertEquals(true, packet.isMidi)
        Assert.assertEquals("test", packet.identifier)
        Assert.assertEquals(
            listOf(
                MidiEvent(
                    type = 1,
                    byteSize = 24,
                    sampleFramesSinceLastEvent = 0,
                    flags = 0,
                    noteLength = 0,
                    noteOffset = 0,
                    midiData = data,
                    reserved = 0,
                    detune = 0,
                    noteOffVelocity = 0,
                )
            ), packet.midiEvents
        )
    }
}

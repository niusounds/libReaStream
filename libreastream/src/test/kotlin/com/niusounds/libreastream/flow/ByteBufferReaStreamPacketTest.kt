package com.niusounds.libreastream.flow

import org.junit.Assert
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random

class ByteBufferReaStreamPacketTest {
    @Test
    fun audioDataTest() {
        val identifier = ByteArray(32)
        identifier[0] = 'd'.code.toByte()
        identifier[1] = 'e'.code.toByte()
        identifier[2] = 'f'.code.toByte()
        identifier[3] = 'a'.code.toByte()
        identifier[4] = 'u'.code.toByte()
        identifier[5] = 'l'.code.toByte()
        identifier[6] = 't'.code.toByte()

        val nch: Byte = 1
        val sampleRate: Int = 44100
        val sblocklen: Short = 1200

        val testSamples = FloatArray(sblocklen.toInt() / Float.SIZE_BYTES)
        repeat(testSamples.size) { i ->
            testSamples[i] = Random.nextFloat()
        }

        val packetSize = 4 + 4 + 32 + 1 + 4 + 2 + sblocklen

        // Create packet raw bytes for testing
        val buffer = ByteBuffer.allocate(packetSize).order(ByteOrder.LITTLE_ENDIAN).apply {
            put('M'.code.toByte())
            put('R'.code.toByte())
            put('S'.code.toByte())
            put('R'.code.toByte())
            putInt(packetSize)
            put(identifier)
            put(nch)
            putInt(sampleRate)
            putShort(sblocklen)

            repeat(testSamples.size) { i ->
                putFloat(testSamples[i])
            }
        }

        // Create ByteBufferReaStreamPacket from raw bytes
        val packet = ByteBufferReaStreamPacket(buffer)

        // Audio data
        Assert.assertEquals(true, packet.isAudio)
        Assert.assertEquals(packetSize, packet.packetSize)
        Assert.assertEquals("default", packet.identifier)
        Assert.assertEquals(nch, packet.channels)
        Assert.assertEquals(sampleRate, packet.sampleRate)
        Assert.assertEquals(sblocklen, packet.blockLength)

        // Audio samples
        val readSamples = FloatArray(packet.blockLength.toInt() / Float.SIZE_BYTES)
        packet.readAudio(readSamples)
        Assert.assertArrayEquals(testSamples, readSamples, 0.000001f)

        // Not MIDI
        Assert.assertEquals(false, packet.isMidi)
    }

    @Test
    fun midiDataTest() {
        val identifier = ByteArray(32)
        identifier[0] = 'd'.code.toByte()
        identifier[1] = 'e'.code.toByte()
        identifier[2] = 'f'.code.toByte()
        identifier[3] = 'a'.code.toByte()
        identifier[4] = 'u'.code.toByte()
        identifier[5] = 'l'.code.toByte()
        identifier[6] = 't'.code.toByte()

        val packetSize = 4 + 4 + 32 + MidiEvent.BYTE_SIZE

        val type = 1
        val byteSize = 4 + 4 + 4 + 4 + 4 + 4 + 3 + 1 + 1 + 1
        val sampleFramesSinceLastEvent = 12345
        val flags = 1
        val noteLength = 42
        val noteOffset = 4242
        val midiData = byteArrayOf(0x80.toByte(), 0x60.toByte(), 0x40.toByte())
        val detune: Byte = 10
        val noteOffVelocity: Byte = 100

        // Create packet raw bytes for testing
        val buffer = ByteBuffer.allocate(packetSize).order(ByteOrder.LITTLE_ENDIAN).apply {
            put('m'.code.toByte())
            put('R'.code.toByte())
            put('S'.code.toByte())
            put('R'.code.toByte())
            putInt(packetSize)
            put(identifier)

            putInt(type)
            putInt(byteSize)
            putInt(sampleFramesSinceLastEvent)
            putInt(flags)
            putInt(noteLength)
            putInt(noteOffset)
            put(midiData)
            put(0) // zero reserved
            put(detune)
            put(noteOffVelocity)
        }

        // Create ByteBufferReaStreamPacket from raw bytes
        val packet = ByteBufferReaStreamPacket(buffer)

        // MIDI data
        Assert.assertEquals(true, packet.isMidi)
        Assert.assertEquals(packetSize, packet.packetSize)
        Assert.assertEquals("default", packet.identifier)
        Assert.assertEquals(
            listOf(
                MidiEvent(
                    type,
                    byteSize,
                    sampleFramesSinceLastEvent,
                    flags,
                    noteLength,
                    noteOffset,
                    midiData,
                    0,
                    detune,
                    noteOffVelocity,
                )
            ), packet.midiEvents
        )

        // Not Audio
        Assert.assertEquals(false, packet.isAudio)
    }
}
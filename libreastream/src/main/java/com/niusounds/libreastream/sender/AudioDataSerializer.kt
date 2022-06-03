package com.niusounds.libreastream.sender

import com.niusounds.libreastream.receiver.ReaStreamPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioDataSerializer(
    private val identifier: String,
    private val sampleRate: Int,
    private val channels: Int,
) {
    private val magicNumberBytes = byteArrayOf(
        'M'.code.toByte(),
        'R'.code.toByte(),
        'S'.code.toByte(),
        'R'.code.toByte(),
    )
    private val identifierBytes = ByteArray(32)

    init {
        check(identifier.length < 32) { "identifier's length must be less than 32" }

        // fill identifierBytes
        identifier.withIndex().forEach { (index, char) ->
            identifierBytes[index] = char.code.toByte()
        }
    }

    /**
     * Serializes PCM [audioData] into [ByteBuffer] which will be parsed into audio samples
     * by remote side ReaStream VST plugin (or libReaStream).
     *
     * [audioData] must not have length larger than 400.
     */
    fun toByteBuffer(audioData: FloatArray): ByteBuffer {
        val sblocklen = audioData.size * Float.SIZE_BYTES

        if (sblocklen <= ReaStreamPacket.MAX_BLOCK_LENGTH) {
            // single packet
            val packetSize = 4 + 4 + 32 + 1 + 4 + 2 + sblocklen

            return ByteBuffer.allocate(packetSize).order(ByteOrder.LITTLE_ENDIAN).apply {
                put(magicNumberBytes)
                putInt(packetSize)
                put(identifierBytes)
                put(channels.toByte())
                putInt(sampleRate)
                putShort(sblocklen.toShort())

                audioData.forEach { sample ->
                    putFloat(sample)
                }

                flip()
            }
        } else {
            // multi packet
            TODO("Multi packet is not supported")
        }
    }
}

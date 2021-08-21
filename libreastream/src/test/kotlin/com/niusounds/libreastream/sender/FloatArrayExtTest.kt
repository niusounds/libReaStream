package com.niusounds.libreastream.sender

import com.niusounds.libreastream.receiver.ByteBufferReaStreamPacket
import org.junit.Assert
import org.junit.Test

class FloatArrayExtTest {
    @Test
    fun testDeInterleave() {
        val data = floatArrayOf(
            1f, 2f, 3f, 4f, 5f, 6f
        )

        val result = data.deInterleave(channels = 2)

        Assert.assertArrayEquals(
            floatArrayOf(
                1f, 3f, 5f, 2f, 4f, 6f,
            ),
            result,
            0.0001f,
        )
    }
}
package com.niusounds.libreastream.ktx

import org.junit.Assert
import org.junit.Test

class FloatArrayExtTest {
    @Test
    fun testDeInterleave() {
        // Interleaved data
        val data = floatArrayOf(
            // L R
            1f, 2f,
            3f, 4f,
            5f, 6f,
        )

        val result = data.deInterleave(channels = 2)

        Assert.assertArrayEquals(
            // Non-interleaved data
            floatArrayOf(
                1f, 3f, 5f, // L
                2f, 4f, 6f, // R
            ),
            result,
            0.0001f,
        )
    }

    @Test
    fun testInterleave() {
        // Non-interleaved data
        val data = floatArrayOf(
            1f, 2f, 3f, // L
            4f, 5f, 6f, // R
        )

        val result = data.interleaved(channels = 2)

        Assert.assertArrayEquals(
            // Interleaved data
            floatArrayOf(
                // L R
                1f, 4f,
                2f, 5f,
                3f, 6f,
            ),
            result,
            0.0001f,
        )
    }
}
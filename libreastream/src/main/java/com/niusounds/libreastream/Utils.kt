package com.niusounds.libreastream

import java.net.InetAddress
import java.net.NetworkInterface

/**
 * Get current network's broadcast address.
 * from https://stackoverflow.com/questions/4887675/detecting-all-available-networks-broadcast-addresses-in-java
 */
fun getBroadcastAddress(): List<InetAddress> {

    val result = mutableListOf<InetAddress>()

    NetworkInterface.getNetworkInterfaces().iterator().forEach { ni ->

        if (!ni.isLoopback && ni.isUp) {

            ni.interfaceAddresses.forEach { address ->

                address.broadcast?.let {
                    result += it
                }
            }
        }
    }

    return result
}

/**
 * This method puts interleaved audio data to outInterleavedAudioData.
 * [original] and [outInterleavedAudioData] should be the same length.
 *
 * @param original Original non interleaved audio data.
 * @param audioDataLength Valid audio data length in original.
 * @param channels Channels in original.
 * @param outInterleavedAudioData Output interleaved audio data.
 */
fun getInterleavedAudioData(original: FloatArray, audioDataLength: Int, channels: Int, outInterleavedAudioData: FloatArray) {

    val samplesPerChannel = audioDataLength / channels

    for (i in 0 until samplesPerChannel) {
        for (ch in 0 until channels) {
            outInterleavedAudioData[i * channels + ch] = original[samplesPerChannel * ch + i]
        }
    }
}

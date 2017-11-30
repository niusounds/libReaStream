package com.eje_c.libreastream

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
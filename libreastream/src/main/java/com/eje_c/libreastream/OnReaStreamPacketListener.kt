package com.eje_c.libreastream

interface OnReaStreamPacketListener {
    fun onReceive(packet: ReaStreamPacket)
}
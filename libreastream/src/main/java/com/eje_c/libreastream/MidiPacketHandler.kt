package com.eje_c.libreastream

interface MidiPacketHandler {

    /**
     * Process audio data.
     */
    fun process(midiEvent: MidiEvent)

    /**
     * Release resources.
     */
    fun release()

    interface Factory {
        fun create(): MidiPacketHandler
    }
}
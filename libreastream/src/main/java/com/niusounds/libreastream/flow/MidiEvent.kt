package com.niusounds.libreastream.flow

data class MidiEvent(
    val type: Int,
    val byteSize: Int,
    val sampleFramesSinceLastEvent: Int,
    val flags: Int,
    val noteLength: Int,
    val noteOffset: Int,
    val midiData: ByteArray,
    private val reserved: Byte,
    val detune: Byte,
    val noteOffVelocity: Byte,
) {

    companion object {
        const val BYTE_SIZE = 4 + 4 + 4 + 4 + 4 + 4 + 3 + 1 + 1 + 1 + 2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiEvent

        if (type != other.type) return false
        if (byteSize != other.byteSize) return false
        if (sampleFramesSinceLastEvent != other.sampleFramesSinceLastEvent) return false
        if (flags != other.flags) return false
        if (noteLength != other.noteLength) return false
        if (noteOffset != other.noteOffset) return false
        if (!midiData.contentEquals(other.midiData)) return false
        if (reserved != other.reserved) return false
        if (detune != other.detune) return false
        if (noteOffVelocity != other.noteOffVelocity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + byteSize
        result = 31 * result + sampleFramesSinceLastEvent
        result = 31 * result + flags
        result = 31 * result + noteLength
        result = 31 * result + noteOffset
        result = 31 * result + midiData.contentHashCode()
        result = 31 * result + reserved
        result = 31 * result + detune
        result = 31 * result + noteOffVelocity
        return result
    }
}

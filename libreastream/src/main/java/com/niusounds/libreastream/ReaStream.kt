package com.niusounds.libreastream

class ReaStream private constructor() {
    companion object {
        /**
         * Port number which is used by ReaStream VST plugin.
         * https://www.reaper.fm/reaplugs/
         */
        const val DEFAULT_PORT = 58710

        /**
         * Default identifier which is used by ReaStream VST plugin.
         * https://www.reaper.fm/reaplugs/
         */
        const val DEFAULT_IDENTIFIER = "default"
    }
}

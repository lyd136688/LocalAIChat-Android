package com.localai.chat.native

import android.os.Build

object DeviceProfileDetector {
    fun detect(): DeviceProfile {
        val board = Build.BOARD.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        
        return when {
            board.contains("dimensity") || hardware.contains("dimensity") -> {
                if (board.contains("9300") || hardware.contains("9300")) {
                    DeviceProfile("Dimensity 9300+", 4096, 512, 8, 0, true, false)
                } else {
                    DeviceProfile("Dimensity", 2048, 256, 4, 0, true, false)
                }
            }
            else -> DeviceProfile("Default", 2048, 256, 4, 0, true, false)
        }
    }
}

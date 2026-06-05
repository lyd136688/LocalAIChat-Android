package com.localai.chat.native

import android.util.Log

object Dimensity9300Config {

    const val TAG = "Dimensity9300"

    val RUNTIME_CONFIG = DeviceProfile(
        name = "Dimensity 9300+",
        nCtx = 8192,
        nBatch = 1024,
        nThreads = 8,
        gpuLayers = 33,
        maxSessionMessages = 200,
        maxSessionTokens = 8192,
        localStorageLimitMB = 2048,
        useNpu = true,
        useGpu = true
    )

    object MemoryConfig {
        const val KV_CACHE_CHUNK_SIZE = 512 * 1024 * 1024L
        const val USE_MMAP = true
        const val USE_MLOCK = false
        const val MEMORY_PRESSURE_LOW = 0.6
        const val MEMORY_PRESSURE_HIGH = 0.85
    }

    object QuantizationConfig {
        const val RECOMMENDED_QUANT = "Q4_K_M"
        val SCENARIO_QUANTS = mapOf(
            "chat" to "Q4_K_M",
            "creative" to "Q5_K_M",
            "code" to "Q4_K_S",
            "long_context" to "Q3_K_M"
        )
    }

    object PerformanceMonitor {
        const val TARGET_TOKENS_PER_SECOND = 15f
        const val THERMAL_NORMAL = 45
        const val THERMAL_WARNING = 55
        const val THERMAL_THROTTLE = 65
    }

    fun getThrottledConfig(temperature: Int): DeviceProfile {
        return when {
            temperature >= PerformanceMonitor.THERMAL_THROTTLE -> {
                Log.w(TAG, "Temperature $temperature°C, heavy throttling")
                DeviceProfile(
                    name = "Dimensity 9300+ (Throttled)",
                    nCtx = 4096,
                    nBatch = 512,
                    nThreads = 4,
                    gpuLayers = 10,
                    maxSessionMessages = 100,
                    maxSessionTokens = 4096,
                    localStorageLimitMB = 2048,
                    useNpu = false,
                    useGpu = true
                )
            }
            temperature >= PerformanceMonitor.THERMAL_WARNING -> {
                Log.w(TAG, "Temperature $temperature°C, light throttling")
                RUNTIME_CONFIG.copy(
                    nThreads = 6,
                    nBatch = 768,
                    gpuLayers = 20
                )
            }
            else -> {
                Log.i(TAG, "Temperature $temperature°C, normal operation")
                RUNTIME_CONFIG
            }
        }
    }
}

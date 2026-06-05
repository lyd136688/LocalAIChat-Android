package com.localai.chat.native

data class DeviceProfile(
    val name: String = "Default",
    val nCtx: Int = 2048,
    val nBatch: Int = 512,
    val nThreads: Int = 4,
    val gpuLayers: Int = 0,
    val maxSessionMessages: Int = 50,
    val maxSessionTokens: Int = 2048,
    val localStorageLimitMB: Long = 500,
    val useNpu: Boolean = false,
    val useGpu: Boolean = false
)

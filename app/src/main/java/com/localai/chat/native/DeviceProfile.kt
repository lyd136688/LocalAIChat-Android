package com.localai.chat.native

data class DeviceProfile(
    val name: String,
    val nCtx: Int,
    val nBatch: Int,
    val nThreads: Int,
    val gpuLayers: Int,
    val useMmap: Boolean,
    val useMlock: Boolean
)

package com.localai.chat.storage

object StorageConfig {
    const val SAFE_MEMORY_THRESHOLD = 20 * 1024 * 1024L
    const val MAX_STORAGE_SIZE = 400L * 1024 * 1024 * 1024
    const val KV_CACHE_SIZE_DEFAULT = 512
    const val KV_CACHE_SIZE_HIGH_END = 2048
}

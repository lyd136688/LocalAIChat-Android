package com.localai.chat.storage

object StorageConfig {
    const val SAFE_THRESHOLD_BYTES = 20 * 1024 * 1024
    const val DB_HARD_LIMIT_BYTES = 24 * 1024 * 1024
    const val PREVIEW_LENGTH = 5000
    const val MEMORY_DIR = "image_memories"
    const val TOTAL_STORAGE_LIMIT_GB = 400L
    const val CLEANUP_TRIGGER_PERCENT = 80
    const val CLEANUP_TARGET_PERCENT = 60
    const val KEEP_RECENT_DAYS = 30
    const val MIN_SESSIONS_TO_KEEP = 10
    const val MAX_SESSION_MESSAGES = 50
    const val MAX_SESSION_TOKENS = 2048
    const val COMPRESS_THRESHOLD_PERCENT = 80
    const val KV_CACHE_LOW_CTX = 1024
    const val KV_CACHE_MID_CTX = 2048
    const val KV_CACHE_HIGH_CTX = 4096
    const val KV_CACHE_DIMENSITY_CTX = 8192
    const val STREAM_CHUNK_SIZE = 1024 * 1024
    const val STREAM_MAX_SAFE_SIZE = 20 * 1024 * 1024
}

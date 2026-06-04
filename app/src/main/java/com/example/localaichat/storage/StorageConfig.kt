package com.example.localaichat.storage

object StorageConfig {
    // 20MB 安全阈值（天玑9300+ 设备）
    const val SAFE_THRESHOLD_BYTES = 20 * 1024 * 1024

    // 24MB 数据库硬上限（Android 14）
    const val DB_HARD_LIMIT_BYTES = 24 * 1024 * 1024

    // 预览长度
    const val PREVIEW_LENGTH = 5000

    // 形象记忆目录
    const val MEMORY_DIR = "image_memories"

    // 总存储上限
    const val TOTAL_STORAGE_LIMIT_GB = 400L

    // 自动清理阈值（达到80%开始清理）
    const val CLEANUP_TRIGGER_PERCENT = 80

    // 清理目标（清理到60%）
    const val CLEANUP_TARGET_PERCENT = 60

    // 保留最近天数
    const val KEEP_RECENT_DAYS = 30

    // 最少保留会话数
    const val MIN_SESSIONS_TO_KEEP = 10

    // 会话上下文配置
    const val MAX_SESSION_MESSAGES = 50       // 单次会话最大消息数
    const val MAX_SESSION_TOKENS = 2048       // 单次会话最大 token 数
    const val COMPRESS_THRESHOLD_PERCENT = 80 // 压缩触发阈值

    // KV-Cache 配置
    const val KV_CACHE_LOW_CTX = 1024         // 低端设备
    const val KV_CACHE_MID_CTX = 2048         // 中端设备
    const val KV_CACHE_HIGH_CTX = 4096        // 高端设备
    const val KV_CACHE_DIMENSITY_CTX = 8192   // 天玑9300+

    // 流式生成配置
    const val STREAM_CHUNK_SIZE = 1024 * 1024  // 1MB 分片
    const val STREAM_MAX_SAFE_SIZE = 20 * 1024 * 1024  // 20MB 安全上限
}

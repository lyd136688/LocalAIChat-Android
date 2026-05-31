package com.ai.localchat

object StorageConfig {
    // 数据库安全阈值：20MB（低于32MB的CursorWindow上限，留足安全余量）
    const val DB_SAFE_THRESHOLD: Long = 20 * 1024 * 1024

    // 本地记忆文件夹名称（App私有目录，无需外部存储权限）
    const val MEMORY_FOLDER_NAME = "local_ai_memory"

    // 本地记忆总容量上限：400GB（适配你的1TB存储，留足系统安全空间）
    const val MAX_MEMORY_TOTAL_SIZE: Long = 400 * 1024 * 1024 * 1024

    // 自动清理时保留最新1000个文件（适配大空间，减少清理频率，保留更多对话）
    const val KEEP_LATEST_FILE_COUNT = 1000

    // 消息预览长度：数据库只存前5000字，远低于20MB安全阈值，不会超限
    const val CONTENT_PREVIEW_LENGTH = 5000
}

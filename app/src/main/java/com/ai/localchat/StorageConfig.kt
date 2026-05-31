package com.ai.localchat

object StorageConfig {
    // 数据库安全阈值：超过20MB自动转存本地文件
    const val DB_SAFE_THRESHOLD: Long = 20 * 1024 * 1024

    // 本地记忆文件夹名称（APP私有目录，无需权限）
    const val MEMORY_FOLDER_NAME = "local_ai_memory"

    // 本地记忆总容量上限：400GB
    const val MAX_MEMORY_TOTAL_SIZE: Long = 400 * 1024 * 1024 * 1024

    // 自动清理时保留最新1000个文件
    const val KEEP_LATEST_FILE_COUNT = 1000

    // 数据库预览文字长度：5000字
    const val CONTENT_PREVIEW_LENGTH = 5000
}


package com.localai.chat.data.models

data class ModelInfo(
    val id: String,
    val displayName: String,
    val parameters: String,
    val size: String,
    val author: String
)

data class MemoryItem(
    val id: Long,
    val content: String,
    val type: String,
    val timestamp: Long
)

data class MessageItem(
    val id: Long,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

data class DownloadTask(
    val id: String,
    val modelName: String,
    val modelId: String,
    val downloadUrl: String,
    val savePath: String,
    val totalSize: Long
)

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

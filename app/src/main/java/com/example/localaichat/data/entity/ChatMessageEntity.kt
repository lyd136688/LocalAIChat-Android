package com.example.localaichat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["isImageMemory"]),
        Index(value = ["topicTag"]),
        Index(value = ["emotionalTag"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val role: String,
    val content: String,              // 小消息=完整内容，大消息=预览
    val fullContentPath: String?,     // 形象记忆文件路径
    val imageTags: String?,           // 形象标签，逗号分隔
    val emotionalTag: String?,        // 情感标签
    val topicTag: String?,            // 主题标签
    val semanticVector: String?,      // 语义向量JSON
    val relatedMemories: String?,     // 关联记忆ID，逗号分隔
    val timestamp: Long,
    val isImageMemory: Boolean = false,
    val isPinned: Boolean = false,
    val accessCount: Int = 0,
    val lastAccessed: Long = 0,
    val fileSize: Long = 0
)

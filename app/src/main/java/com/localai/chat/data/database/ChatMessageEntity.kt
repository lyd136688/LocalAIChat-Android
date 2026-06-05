package com.localai.chat.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val content: String,
    val fullContentPath: String? = null,
    val imageTags: String? = null,
    val emotionalTag: String? = null,
    val topicTag: String? = null,
    val semanticVector: String? = null,
    val relatedMemories: String? = null,
    val timestamp: Long,
    val isImageMemory: Boolean = false,
    val isPinned: Boolean = false,
    val accessCount: Int = 0,
    val lastAccessed: Long = 0,
    val fileSize: Long = 0,
    val isCleaned: Boolean = false
)

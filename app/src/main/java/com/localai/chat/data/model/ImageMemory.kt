package com.localai.chat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageMemory(
    val id: String,
    val content: String,
    val imageTags: List<String>,
    val semanticVector: List<Float>,
    val emotionalTag: String,
    val topicTag: String,
    val relatedMemories: List<String>,
    val createdAt: Long,
    val accessCount: Int = 0,
    val lastAccessed: Long = 0
)

data class MemorySearchResult(
    val memory: com.localai.chat.data.database.ChatMessageEntity,
    val similarity: Float,
    val fullContent: String? = null
)

package com.example.localaichat.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageMemory(
    val id: String,
    val content: String,                    // 完整内容
    val imageTags: List<String>,            // 形象标签
    val semanticVector: List<Float>,        // 语义向量
    val emotionalTag: String,               // 情感标签
    val topicTag: String,                   // 主题标签
    val relatedMemories: List<String>,      // 关联记忆ID
    val createdAt: Long,
    val accessCount: Int = 0,
    val lastAccessed: Long = 0
)

data class MemorySearchResult(
    val memory: ChatMessageEntity,
    val similarity: Float,
    val fullContent: String? = null
)

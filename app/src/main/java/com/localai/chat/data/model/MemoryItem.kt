package com.localai.chat.data.model

data class MemoryItem(
    val id: String,
    val content: String,
    val type: String,
    val source: String,
    val createdAt: Long,
    val tags: List<String> = emptyList(),
    val relevanceScore: Float = 0f
)

data class MessageItem(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val imageUrl: String? = null,
    val status: String = "completed"
)

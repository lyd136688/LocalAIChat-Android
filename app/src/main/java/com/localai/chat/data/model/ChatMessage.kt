package com.localai.chat.data.model

data class ChatMessage(
    val id: String,
    val sessionId: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val imageUrl: String? = null
)

package com.localai.chat.ui.model

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: String
)

package com.localai.chat.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: Long,
    val sessionId: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long
)

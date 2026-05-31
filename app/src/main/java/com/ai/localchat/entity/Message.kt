package com.ai.localchat.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 消息类型：文本/图片/音频
enum class MsgType {
    TEXT,
    IMAGE,
    AUDIO
}

@Entity(tableName = "chat_messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String = "",
    val contentPreview: String = "",
    val contentTotalLen: Long = 0L,
    val msgType: MsgType = MsgType.TEXT,
    val fileAbsolutePath: String? = null,
    val timeStamp: Long = System.currentTimeMillis()
)


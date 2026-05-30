package com.ai.localchat.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 对话记忆实体（长期本地存储） */
@Entity(tableName = "chat_record")
data class ChatRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createTime: Long,
    val userText: String,
    val aiText: String,
    val mediaPath: String?, // 图片/视频/文件本地路径
    val agentName: String   // 本条对话使用的Agent
)


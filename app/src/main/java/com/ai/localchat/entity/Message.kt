package com.ai.localchat.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 消息类型枚举，适配你的多模态聊天需求
enum class MsgType {
    TEXT,   // 文本消息
    IMAGE,  // 图片消息
    AUDIO   // 语音消息
}

/**
 * 适配「数据库+文件双存储」的消息实体类
 * 数据库只存：预览文本、文件路径、基础信息
 * 超大内容自动存到App私有文件，彻底解决Row too big崩溃
 */
@Entity(tableName = "chat_messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // 消息角色：user（用户）/ assistant（AI）/ system（系统提示词）
    val role: String = "",

    // 预览文本：数据库只存前5000字（和你设置的StorageConfig一致）
    // 足够在聊天列表里展示上下文，又不会触发数据库超限
    val contentPreview: String = "",

    // 原始内容总长度：用于判断是否需要转存文件
    val contentTotalLen: Long = 0L,

    // 消息类型：文本/图片/音频，适配你的多模态需求
    val msgType: MsgType = MsgType.TEXT,

    // 超大内容文件路径：
    // - 文本消息：超过20MB时，完整内容存到本地文件，这里存文件路径
    // - 图片/音频消息：不存Base64，直接存文件路径，避免数据库爆炸
    val fileAbsolutePath: String? = null,

    // 消息时间戳：用于聊天列表排序、自动清理旧文件
    val timeStamp: Long = System.currentTimeMillis()
)


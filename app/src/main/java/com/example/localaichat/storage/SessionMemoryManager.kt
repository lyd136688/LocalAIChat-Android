package com.example.localaichat.storage

import android.util.Log
import com.example.localaichat.data.entity.ChatMessageEntity
import com.example.localaichat.data.model.ChatMessage

class SessionMemoryManager(
    private val maxMessages: Int = StorageConfig.MAX_SESSION_MESSAGES,
    private val maxTokens: Int = StorageConfig.MAX_SESSION_TOKENS
) {
    companion object {
        const val TAG = "SessionMemoryManager"
    }

    private val messages = mutableListOf<ChatMessage>()

    /**
     * 添加消息，自动管理上下文窗口
     */
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        trimContext()
    }

    /**
     * 获取当前上下文（用于发送给模型）
     */
    fun getContext(): List<ChatMessage> {
        return messages.toList()
    }

    /**
     * 获取格式化的上下文文本
     */
    fun getFormattedContext(): String {
        val sb = StringBuilder()
        messages.forEach { msg ->
            when (msg.role) {
                "system" -> sb.append("系统: ${msg.content}\n")
                "user" -> sb.append("用户: ${msg.content}\n")
                "assistant" -> sb.append("助手: ${msg.content}\n")
            }
        }
        return sb.toString()
    }

    /**
     * 估算当前 token 数
     */
    fun estimateTokens(): Int {
        // 中文约 1.5 字/token，英文约 4 字符/token
        return messages.sumOf { msg ->
            val chineseChars = msg.content.count { it.code in 0x4E00..0x9FFF }
            val otherChars = msg.content.length - chineseChars
            (chineseChars * 1.5 + otherChars / 4.0).toInt()
        }
    }

    /**
     * 智能裁剪上下文
     */
    private fun trimContext() {
        // 策略1: 消息数量限制
        while (messages.size > maxMessages) {
            val removed = messages.removeAt(0)
            Log.d(TAG, "Removed old message (count limit): ${removed.content.take(50)}...")
        }

        // 策略2: Token 数限制
        val estimatedTokens = estimateTokens()
        val threshold = (maxTokens * StorageConfig.COMPRESS_THRESHOLD_PERCENT / 100.0).toInt()

        if (estimatedTokens > threshold) {
            compressOldMessages()
        }
    }

    /**
     * 压缩旧消息：将早期对话总结成一条系统消息
     */
    private fun compressOldMessages() {
        if (messages.size <= 5) return // 保留最近5条不压缩

        val oldCount = messages.size / 2
        val oldMessages = messages.take(oldCount)
        val recentMessages = messages.drop(oldCount)

        // 生成摘要
        val summary = generateSummary(oldMessages)

        // 替换旧消息为摘要
        messages.clear()
        messages.add(ChatMessage(
            id = "summary_${System.currentTimeMillis()}",
            sessionId = oldMessages.firstOrNull()?.sessionId ?: "",
            role = "system",
            content = "[历史对话摘要] 之前讨论了${oldCount}条消息，主要内容：$summary",
            timestamp = oldMessages.firstOrNull()?.timestamp ?: 0
        ))
        messages.addAll(recentMessages)

        Log.i(TAG, "Compressed $oldCount messages into summary")
    }

    /**
     * 简单摘要生成（实际应用可用模型生成）
     */
    private fun generateSummary(messages: List<ChatMessage>): String {
        val topics = mutableSetOf<String>()
        messages.forEach { msg ->
            when {
                msg.content.contains("代码") || msg.content.contains("编程") -> topics.add("编程相关")
                msg.content.contains("诗") || msg.content.contains("故事") -> topics.add("创作内容")
                msg.content.contains("怎么") || msg.content.contains("为什么") -> topics.add("问答交流")
                msg.content.length > 100 -> topics.add("长对话")
            }
        }
        return "涉及话题：${topics.joinToString("、")}，共${messages.size}条消息"
    }

    /**
     * 清空会话
     */
    fun clear() {
        messages.clear()
    }

    /**
     * 加载历史消息
     */
    fun loadHistory(entities: List<ChatMessageEntity>) {
        messages.clear()
        entities.forEach { entity ->
            messages.add(ChatMessage(
                id = entity.id,
                sessionId = entity.sessionId,
                role = entity.role,
                content = entity.content,
                timestamp = entity.timestamp
            ))
        }
        trimContext()
    }
}


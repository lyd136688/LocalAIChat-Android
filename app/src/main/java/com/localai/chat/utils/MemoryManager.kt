package com.localai.chat.utils

import android.content.Context
import com.localai.chat.data.database.AppDatabase
import com.localai.chat.data.database.MemoryEntity
import com.localai.chat.data.models.MemoryItem
import com.localai.chat.data.models.MessageItem
import kotlinx.coroutines.flow.first
import java.util.UUID

class MemoryManager(private val context: Context, private val database: AppDatabase) {

    suspend fun addShortTermMemory(content: String, source: String) {
        database.memoryDao().insert(MemoryEntity(
            UUID.randomUUID().toString(),
            content,
            "short_term",
            source,
            sessionId = getCurrentSessionId()
        ))
    }

    suspend fun getShortTermMemories(): List<MemoryItem> {
        return database.memoryDao().getByType("short_term").first().map { it.toMemoryItem() }
    }

    suspend fun getLongTermMemories(): List<MemoryItem> {
        return database.memoryDao().getByType("long_term").first().map { it.toMemoryItem() }
    }

    suspend fun getRecentMessages(limit: Int): List<MessageItem> {
        return database.messageDao().getRecent(limit).first().map { it.toMessageItem() }
    }

    suspend fun archiveToLongTerm(id: String) {
        database.memoryDao().archiveToLongTerm(id, System.currentTimeMillis())
    }

    suspend fun deleteMemory(id: String) {
        database.memoryDao().getById(id)?.let { database.memoryDao().delete(it) }
    }

    suspend fun searchMemories(query: String): List<MemoryItem> {
        return database.memoryDao().search("%$query%").first().map { it.toMemoryItem() }
    }

    suspend def getMemoriesByTag(tag: String): List<MemoryItem> {
        return database.memoryDao().getByTag(tag).first().map { it.toMemoryItem() }
    }

    private fun getCurrentSessionId(): String {
        val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        var sessionId = prefs.getString("current_session", null)
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString()
            prefs.edit().putString("current_session", sessionId).apply()
        }
        return sessionId
    }

    private fun MemoryEntity.toMemoryItem() = MemoryItem(
        id, content, type, source, createdAt,
        tags?.split(",") ?: emptyList(), relevanceScore
    )

    private fun com.localai.chat.data.database.MessageEntity.toMessageItem() = MessageItem(
        id, content, isUser, timestamp, imageUrl, status
    )
}


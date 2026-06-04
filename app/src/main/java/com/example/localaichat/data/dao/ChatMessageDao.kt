package com.example.localaichat.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.localaichat.data.entity.ChatMessageEntity

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE id = :messageId")
    suspend fun getById(messageId: String): ChatMessageEntity?

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp")
    suspend fun getSessionMessages(sessionId: String): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE isImageMemory = 1")
    suspend fun getAllImageMemories(): List<ChatMessageEntity>

    @Query("UPDATE chat_messages SET accessCount = accessCount + 1, lastAccessed = :timestamp WHERE id = :messageId")
    suspend fun incrementAccessCount(messageId: String, timestamp: Long)

    @Query("SELECT isPinned FROM chat_messages WHERE id = :messageId")
    suspend fun isPinned(messageId: String): Boolean

    @Query("UPDATE chat_messages SET fullContentPath = NULL, isCleaned = 1 WHERE id = :messageId")
    suspend fun markAsCleaned(messageId: String)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId AND timestamp < :beforeTime AND isPinned = 0")
    suspend fun deleteOldMessages(sessionId: String, beforeTime: Long)

    @Query("SELECT SUM(fileSize) FROM chat_messages WHERE isImageMemory = 1")
    suspend fun getTotalImageMemorySize(): Long?

    @Query("SELECT * FROM chat_messages WHERE topicTag = :topic ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getMessagesByTopic(topic: String, limit: Int): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE emotionalTag = :emotion ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getMessagesByEmotion(emotion: String, limit: Int): List<ChatMessageEntity>
}

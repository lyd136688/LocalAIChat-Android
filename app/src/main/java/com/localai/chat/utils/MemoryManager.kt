package com.localai.chat.utils

import com.localai.chat.data.database.MemoryEntity
import com.localai.chat.data.models.MemoryItem
import kotlinx.coroutines.flow.first

class MemoryManager(private val context: android.content.Context) {
    
    companion object {
        const val TYPE_SHORT_TERM = "short_term"
        const val TYPE_LONG_TERM = "long_term"
        private const val MAX_SHORT_TERM = 50
        private const val MAX_LONG_TERM = 200
    }
    
    private val memoryDao = com.localai.chat.MyApplication.instance.database.memoryDao()
    
    suspend fun addMemory(content: String, type: String = TYPE_SHORT_TERM): MemoryItem {
        val id = System.currentTimeMillis()
        val entity = MemoryEntity(
            id = id,
            content = content,
            type = type,
            timestamp = id
        )
        memoryDao.insertMemory(entity)
        
        if (type == TYPE_SHORT_TERM) {
            trimMemories(TYPE_SHORT_TERM, MAX_SHORT_TERM)
        }
        
        return MemoryItem(id, content, type, id)
    }
    
    suspend fun getShortTermMemories(): List<MemoryItem> {
        return memoryDao.getMemoriesByType(TYPE_SHORT_TERM).first().map { entity ->
            MemoryItem(entity.id, entity.content, entity.type, entity.timestamp)
        }
    }
    
    suspend fun getLongTermMemories(): List<MemoryItem> {
        return memoryDao.getMemoriesByType(TYPE_LONG_TERM).first().map { entity ->
            MemoryItem(entity.id, entity.content, entity.type, entity.timestamp)
        }
    }
    
    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteMemory(id)
    }
    
    suspend fun archiveMemory(id: Long) {
        memoryDao.archiveMemory(id)
    }
    
    suspend fun searchMemory(query: String): List<MemoryItem> {
        val allMemories = memoryDao.getMemoriesByType(TYPE_SHORT_TERM).first() +
            memoryDao.getMemoriesByType(TYPE_LONG_TERM).first()
        
        return allMemories
            .filter { it.content.contains(query, ignoreCase = true) }
            .map { MemoryItem(it.id, it.content, it.type, it.timestamp) }
    }
    
    suspend fun promoteToLongTerm(id: Long) {
        val entity = memoryDao.getMemoryById(id) ?: return
        val updated = entity.copy(type = TYPE_LONG_TERM)
        memoryDao.updateMemory(updated)
    }
    
    private suspend fun trimMemories(type: String, maxCount: Int) {
        val memories = memoryDao.getMemoriesByType(type).first()
        if (memories.size > maxCount) {
            val toDelete = memories.dropLast(maxCount)
            toDelete.forEach { memoryDao.deleteMemory(it.id) }
        }
    }
}

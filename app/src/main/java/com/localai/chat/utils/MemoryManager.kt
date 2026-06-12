package com.localai.chat.utils

import android.content.Context
import com.localai.chat.data.database.AppDatabase
import com.localai.chat.data.database.MemoryEntity
import com.localai.chat.data.models.MemoryItem
import kotlin.math.sqrt

class MemoryManager(private val context: Context, private val database: AppDatabase) {

    // --- 写入：记忆 + 本地向量（字符 n-gram，稳定哈希，不做压缩） ---

    suspend fun addMemory(content: String, source: String = "chat") {
        val vector = vectorize(content)
        val entity = MemoryEntity(
            id = java.util.UUID.randomUUID().toString(),
            content = content,
            type = "vector",
            source = source,
            sessionId = getCurrentSessionId(),
            tags = source,
            embedding = vector.joinToString(separator = ","),
            createdAt = System.currentTimeMillis(),
            archivedAt = null,
            relevanceScore = 0f
        )
        database.memoryDao().insert(entity)
    }

    suspend fun getAllMemoriesOrderedByTime(): List<MemoryItem> {
        return database.memoryDao().getRecent(500).map { entity ->
            MemoryItem(
                id = entity.id,
                content = entity.content,
                type = entity.type,
                source = entity.source,
                createdAt = entity.createdAt,
                tags = entity.tags?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                relevanceScore = entity.relevanceScore
            )
        }
    }

    // --- 语义检索：余弦相似度 top-K，原文不压缩 ---

    suspend fun searchMemoriesByVector(query: String, topK: Int = 20): List<MemoryItem> {
        val queryVec = vectorize(query)
        val all = database.memoryDao().getRecent(500)

        val scored = all.mapNotNull { entity ->
            val embeddingStr = entity.embedding
            if (embeddingStr.isNullOrEmpty()) null
            else {
                val vec = embeddingStr.split(",").mapNotNull { it.trim().toFloatOrNull() }
                if (vec.size != VECTOR_DIM) null
                else {
                    val score = cosineSimilarity(queryVec, vec)
                    Triple(entity, score, vec)
                }
            }
        }.sortedByDescending { it.second }
         .take(topK)

        return scored.map { (entity, score, _) ->
            MemoryItem(
                id = entity.id,
                content = entity.content,
                type = entity.type,
                source = entity.source,
                createdAt = entity.createdAt,
                tags = entity.tags?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                relevanceScore = score
            )
        }
    }

    suspend fun searchMemories(query: String): List<MemoryItem> = searchMemoriesByVector(query)

    suspend fun deleteMemory(id: String) {
        database.memoryDao().getById(id)?.let { database.memoryDao().delete(it) }
    }

    // --- 旧方法兼容保留 ---

    suspend fun addShortTermMemory(content: String, source: String) {
        addMemory(content, source)
    }

    suspend fun getShortTermMemories(): List<MemoryItem> = getAllMemoriesOrderedByTime()
    suspend fun getLongTermMemories(): List<MemoryItem> = getAllMemoriesOrderedByTime()

    suspend fun archiveToLongTerm(@Suppress("UNUSED_PARAMETER") id: String) {
        // 按新需求：不做压缩归档，不做短期→长期转换
    }

    private fun getCurrentSessionId(): String {
        val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        var sessionId = prefs.getString("current_session", null)
        if (sessionId == null) {
            sessionId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString("current_session", sessionId).apply()
        }
        return sessionId
    }

    // --- 简易本地向量：字符 n-gram + 稳定哈希 + L2 归一化 ---

    private companion object {
        const val VECTOR_DIM = 64
    }

    private fun vectorize(text: String): FloatArray {
        val normalized = text.lowercase()
            .replace("[\r\n\t]+".toRegex(), " ")
            .replace("[^\\u4e00-\\u9fa5a-z0-9 ]+".toRegex(), "")
        val vec = FloatArray(VECTOR_DIM) { 0f }

        if (normalized.isEmpty()) return vec

        val charList = normalized.toList()
        for (i in charList.indices) {
            val idx1 = stableHash(charList[i].toString())
            vec[idx1] += 1f
            if (i < charList.size - 1) {
                val bi = charList[i].toString() + charList[i + 1].toString()
                val idx2 = stableHash(bi)
                vec[idx2] += 0.8f
            }
        }

        // L2 归一化
        var norm = 0f
        for (v in vec) norm += v * v
        norm = sqrt(norm)
        if (norm > 0f) for (i in vec.indices) vec[i] = vec[i] / norm
        return vec
    }

    private fun stableHash(token: String): Int {
        var h = 0
        for (c in token) {
            h = 31 * h + c.code
        }
        // 映射到 [0, VECTOR_DIM)，确保非负
        val mod = h % VECTOR_DIM
        return if (mod < 0) mod + VECTOR_DIM else mod
    }

    private fun cosineSimilarity(a: FloatArray, b: List<Float>): Float {
        if (a.size != b.size) return 0f
        var dot = 0f
        for (i in a.indices) dot += a[i] * b[i]
        return dot
    }
}


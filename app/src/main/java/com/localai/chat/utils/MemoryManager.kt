package com.localai.chat.utils

import android.content.Context
import com.localai.chat.data.database.AppDatabase
import com.localai.chat.data.database.MemoryEntity
import com.localai.chat.data.models.MemoryItem
import kotlinx.coroutines.flow.first
import kotlin.math.sqrt
import java.util.UUID

/**
 * 本地记忆管理器
 *
 * 核心特性：
 * - 所有记忆原始内容完整保留，不做压缩归档
 * - 新增记忆时，自动生成基于字符 n-gram 的轻量向量
 * - 检索优先使用向量相似度排序；作为 fallback 支持关键字模糊匹配
 * - 提供短期/长期记忆的分层读取接口
 */
class MemoryManager(private val context: Context, private val database: AppDatabase) {

    companion object {
        const val VECTOR_DIM = 128
        const val MIN_SIMILARITY = 0.15f
    }

    suspend fun addShortTermMemory(content: String, source: String) {
        val embedding = vectorizeAndJoin(content)
        database.memoryDao().insert(
            MemoryEntity(
                id = UUID.randomUUID().toString(),
                content = content,
                type = "short_term",
                source = source,
                sessionId = getCurrentSessionId(),
                tags = null,
                embedding = embedding,
                createdAt = System.currentTimeMillis(),
                archivedAt = null,
                relevanceScore = 0f
            )
        )
    }

    suspend fun addLongTermMemory(content: String, source: String, tags: List<String>? = null) {
        val embedding = vectorizeAndJoin(content)
        database.memoryDao().insert(
            MemoryEntity(
                id = UUID.randomUUID().toString(),
                content = content,
                type = "long_term",
                source = source,
                sessionId = null,
                tags = tags?.joinToString(","),
                embedding = embedding,
                createdAt = System.currentTimeMillis(),
                archivedAt = System.currentTimeMillis(),
                relevanceScore = 0f
            )
        )
    }

    suspend fun getShortTermMemories(): List<MemoryItem> {
        return database.memoryDao().getByType("short_term").first().map { it.toMemoryItem(0f) }
    }

    suspend fun getLongTermMemories(): List<MemoryItem> {
        return database.memoryDao().getByType("long_term").first().map { it.toMemoryItem(0f) }
    }

    suspend fun getRecentMemories(limit: Int = 50): List<MemoryItem> {
        return database.memoryDao().getRecent(limit).first().map { it.toMemoryItem(0f) }
    }

    /**
     * 向量检索：字符 n-gram 向量 + 余弦相似度
     */
    suspend fun searchMemoriesByVector(query: String, topK: Int = 30): List<MemoryItem> {
        val qVec = vectorize(query)
        val all = database.memoryDao().getRecent(500).first()

        val scored: List<Pair<MemoryEntity, Float>> = all.mapNotNull { entity ->
            val embeddingStr = entity.embedding
            if (embeddingStr.isNullOrEmpty()) null
            else {
                val vec = parseEmbedding(embeddingStr)
                if (vec.size != VECTOR_DIM) null
                else Pair(entity, cosineSimilarity(qVec, vec))
            }
        }.sortedByDescending { it.second }

        // 关键字 fallback
        val keywordMatches = mutableListOf<MemoryEntity>()
        if (query.isNotBlank()) {
            for (e in all) {
                if (e.content.contains(query, ignoreCase = true)) {
                    if (scored.none { it.first.id == e.id }) keywordMatches.add(e)
                }
            }
        }

        val vectorResult = scored.take(topK).map { (entity, score) -> entity.toMemoryItem(score) }
        val keywordResult = keywordMatches.take(5).map { it.toMemoryItem(0.1f) }
        return (vectorResult + keywordResult).distinctBy { it.id }.take(topK)
    }

    suspend fun searchMemories(query: String): List<MemoryItem> {
        if (query.isBlank()) return getRecentMemories(30)
        return searchMemoriesByVector(query, 30)
    }

    suspend fun archiveToLongTerm(id: String) {
        database.memoryDao().archiveToLongTerm(id, System.currentTimeMillis())
    }

    suspend fun deleteMemory(id: String) {
        database.memoryDao().getById(id)?.let { database.memoryDao().delete(it) }
    }

    suspend fun clearShortTerm() {
        val list = database.memoryDao().getByType("short_term").first()
        for (e in list) database.memoryDao().delete(e)
    }

    suspend fun getStats(): MemoryStats {
        val short = database.memoryDao().getByType("short_term").first().size
        val long = database.memoryDao().getByType("long_term").first().size
        return MemoryStats(shortTermCount = short, longTermCount = long)
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

    /** 字符 n-gram 向量化（L2 归一化） */
    private fun vectorize(text: String): FloatArray {
        val normalized = text
            .lowercase()
            .replace("[\r\n\t]+".toRegex(), " ")
            .replace("[^\\u4e00-\\u9fa5a-z0-9 ]+".toRegex(), "")

        val vec = FloatArray(VECTOR_DIM) { 0f }
        if (normalized.isBlank()) return vec

        val chars = normalized.toCharArray()
        for (i in chars.indices) {
            val uni = chars[i].toString()
            vec[stableHash(uni)] += 1f
            if (i < chars.size - 1 && chars[i] != ' ' && chars[i + 1] != ' ') {
                val bi = uni + chars[i + 1]
                vec[stableHash(bi)] += 0.8f
            }
        }

        var norm = 0f
        for (v in vec) norm += v * v
        norm = sqrt(norm)
        if (norm > 0f) for (i in vec.indices) vec[i] = vec[i] / norm
        return vec
    }

    private fun vectorizeAndJoin(text: String): String {
        return vectorize(text).joinToString(",")
    }

    private fun parseEmbedding(embeddingStr: String): FloatArray {
        val parts = embeddingStr.split(",")
        val out = FloatArray(VECTOR_DIM) { 0f }
        var i = 0
        while (i < parts.size && i < VECTOR_DIM) {
            out[i] = parts[i].trim().toFloatOrNull() ?: 0f
            i++
        }
        return out
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        var na = 0f
        var nb = 0f
        val n = minOf(a.size, b.size)
        for (i in 0 until n) {
            dot += a[i] * b[i]
            na += a[i] * a[i]
            nb += b[i] * b[i]
        }
        if (na == 0f || nb == 0f) return 0f
        val score = dot / (sqrt(na) * sqrt(nb))
        return if (score > MIN_SIMILARITY) score else 0f
    }

    private fun stableHash(token: String): Int {
        var h = 1125899906842597L
        for (c in token) h = h * 31 + c.code.toLong()
        h = h xor (h ushr 33)
        h *= 0xff51afd7ed558ccdL
        h = h xor (h ushr 33)
        h *= 0xc4ceb9fe1a85ec53L
        h = h xor (h ushr 33)
        val mod = ((h % VECTOR_DIM) + VECTOR_DIM) % VECTOR_DIM
        return mod.toInt()
    }

    private fun MemoryEntity.toMemoryItem(score: Float): MemoryItem {
        return MemoryItem(
            id = this.id,
            content = this.content,
            type = this.type,
            source = this.source,
            createdAt = this.createdAt,
            tags = this.tags?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            relevanceScore = score
        )
    }
}

data class MemoryStats(
    val shortTermCount: Int,
    val longTermCount: Int
)


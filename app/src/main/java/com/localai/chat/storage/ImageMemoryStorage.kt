package com.localai.chat.storage

import android.content.Context
import android.util.Log
import com.localai.chat.data.database.ChatMessageDao
import com.localai.chat.data.database.ChatMessageEntity
import com.localai.chat.data.model.ChatMessage
import com.localai.chat.data.model.ImageMemory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.sqrt

class ImageMemoryStorage(
    private val context: Context,
    private val messageDao: ChatMessageDao
) {
    companion object {
        const val SAFE_THRESHOLD_BYTES = StorageConfig.SAFE_THRESHOLD_BYTES
        const val PREVIEW_LENGTH = StorageConfig.PREVIEW_LENGTH
        const val MEMORY_DIR = StorageConfig.MEMORY_DIR
        const val TOTAL_STORAGE_LIMIT_GB = StorageConfig.TOTAL_STORAGE_LIMIT_GB
        const val TAG = "ImageMemoryStorage"
    }

    private val memoryDir = File(context.filesDir, MEMORY_DIR).apply { mkdirs() }

    suspend fun saveMessage(message: ChatMessage): String {
        val contentBytes = message.content.toByteArray(Charsets.UTF_8)
        return if (contentBytes.size >= SAFE_THRESHOLD_BYTES) {
            Log.i(TAG, "Message ${message.id} exceeded ${SAFE_THRESHOLD_BYTES} bytes, saving to image memory")
            saveToImageMemory(message)
        } else {
            saveToActiveMemory(message)
        }
    }

    private suspend fun saveToActiveMemory(message: ChatMessage): String {
        val entity = ChatMessageEntity(
            id = message.id,
            sessionId = message.sessionId,
            role = message.role,
            content = message.content,
            fullContentPath = null,
            imageTags = null,
            emotionalTag = null,
            topicTag = null,
            semanticVector = null,
            relatedMemories = null,
            timestamp = message.timestamp,
            isImageMemory = false,
            fileSize = message.content.toByteArray().size.toLong()
        )
        messageDao.insert(entity)
        return message.id
    }

    private suspend fun saveToImageMemory(message: ChatMessage): String {
        val imageTags = extractImageTags(message.content)
        val emotionalTag = analyzeEmotion(message.content)
        val topicTag = classifyTopic(message.content)
        val semanticVector = generateSemanticVector(message.content)
        val relatedMemories = findRelatedMemories(semanticVector)

        val fileName = "img_mem_${message.id}_${System.currentTimeMillis()}.json"
        val file = File(memoryDir, fileName)

        val imageMemory = ImageMemory(
            id = message.id,
            content = message.content,
            imageTags = imageTags,
            semanticVector = semanticVector.toList(),
            emotionalTag = emotionalTag,
            topicTag = topicTag,
            relatedMemories = relatedMemories,
            createdAt = System.currentTimeMillis()
        )

        file.writeText(Json.encodeToString(imageMemory))

        val preview = message.content.take(PREVIEW_LENGTH)
        val entity = ChatMessageEntity(
            id = message.id,
            sessionId = message.sessionId,
            role = message.role,
            content = preview,
            fullContentPath = file.absolutePath,
            imageTags = imageTags.joinToString(","),
            emotionalTag = emotionalTag,
            topicTag = topicTag,
            semanticVector = Json.encodeToString(semanticVector.toList()),
            relatedMemories = relatedMemories.joinToString(","),
            timestamp = message.timestamp,
            isImageMemory = true,
            fileSize = file.length()
        )

        messageDao.insert(entity)
        cleanupOldFilesIfNeeded()
        return message.id
    }

    suspend fun getFullContent(messageId: String): String? {
        val entity = messageDao.getById(messageId) ?: return null
        messageDao.incrementAccessCount(messageId, System.currentTimeMillis())
        return if (entity.isImageMemory) {
            readFromImageMemory(entity)
        } else {
            entity.content
        }
    }

    private fun readFromImageMemory(entity: ChatMessageEntity): String? {
        val path = entity.fullContentPath ?: return entity.content
        val file = File(path)
        return if (file.exists()) {
            try {
                val imageMemory = Json.decodeFromString<ImageMemory>(file.readText())
                imageMemory.content
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read image memory file", e)
                entity.content + "\n\n[完整内容读取失败]"
            }
        } else {
            entity.content + "\n\n[完整内容已迁移或清理]"
        }
    }

    suspend fun searchImageMemory(query: String): List<com.localai.chat.data.model.MemorySearchResult> {
        val queryVector = generateSemanticVector(query)
        val queryTags = extractImageTags(query)
        val allMemories = messageDao.getAllImageMemories()

        val results = allMemories.map { memory ->
            val memoryVector = parseSemanticVector(memory.semanticVector)
            val similarity = cosineSimilarity(queryVector, memoryVector)
            val memoryTags = memory.imageTags?.split(",") ?: emptyList()
            val tagBonus = if (memoryTags.any { it in queryTags }) 0.3f else 0f
            val freqBonus = (memory.accessCount / 1000f) * 0.1f
            val finalScore = similarity * 0.6f + tagBonus + freqBonus

            com.localai.chat.data.model.MemorySearchResult(
                memory = memory,
                similarity = finalScore,
                fullContent = getFullContent(memory.id)
            )
        }

        return results.sortedByDescending { it.similarity }
    }

    private suspend fun cleanupOldFilesIfNeeded() {
        val totalSize = calculateTotalStorageSize()
        val limitBytes = TOTAL_STORAGE_LIMIT_GB * 1024 * 1024 * 1024

        if (totalSize > limitBytes) {
            Log.i(TAG, "Storage limit exceeded, cleaning up old files")
            val files = memoryDir.listFiles()?.sortedBy { it.lastModified() } ?: return
            var currentSize = totalSize
            for (file in files) {
                if (currentSize <= limitBytes * 0.8) break
                val messageId = extractMessageId(file.name)
                if (!messageDao.isPinned(messageId)) {
                    val fileSize = file.length()
                    file.delete()
                    currentSize -= fileSize
                    messageDao.markAsCleaned(messageId)
                }
            }
        }
    }

    private fun extractImageTags(content: String): List<String> {
        val keywords = mutableListOf<String>()
        if (content.contains("诗") || content.contains("歌") || content.contains("写")) {
            keywords.addAll(listOf("诗", "创作", "文艺"))
        }
        if (content.contains("代码") || content.contains("程序") || content.contains("bug")) {
            keywords.addAll(listOf("编程", "技术", "逻辑"))
        }
        if (content.contains("爱") || content.contains("喜欢") || content.contains("感情")) {
            keywords.addAll(listOf("情感", "温馨", "亲密"))
        }
        if (content.contains("为什么") || content.contains("怎么") || content.contains("什么")) {
            keywords.addAll(listOf("问答", "知识", "探索"))
        }
        val words = content.split(Regex("\\s+"))
        val wordFreq = words.groupingBy { it }.eachCount()
        val topWords = wordFreq.entries.sortedByDescending { it.value }.take(5).map { it.key }
        keywords.addAll(topWords)
        return keywords.distinct().take(20)
    }

    private fun analyzeEmotion(content: String): String {
        return when {
            content.contains("开心") || content.contains("高兴") || content.contains("棒") -> "愉悦"
            content.contains("难过") || content.contains("伤心") || content.contains("哭") -> "忧伤"
            content.contains("生气") || content.contains("愤怒") || content.contains("讨厌") -> "愤怒"
            content.contains("害怕") || content.contains("担心") || content.contains("恐惧") -> "恐惧"
            content.contains("惊讶") || content.contains("震惊") || content.contains("居然") -> "惊讶"
            else -> "平静"
        }
    }

    private fun classifyTopic(content: String): String {
        return when {
            content.contains("代码") || content.contains("编程") || content.contains("算法") -> "技术"
            content.contains("诗") || content.contains("故事") || content.contains("小说") -> "创作"
            content.contains("怎么") || content.contains("为什么") || content.contains("如何") -> "问答"
            content.contains("建议") || content.contains("推荐") || content.contains("最好") -> "建议"
            else -> "聊天"
        }
    }

    private fun generateSemanticVector(content: String): FloatArray {
        val dimension = 128
        val vector = FloatArray(dimension) { 0f }
        val words = content.split(Regex("\\s+"))
        words.forEachIndexed { _, word ->
            val hash = word.hashCode()
            val idx = kotlin.math.abs(hash % dimension)
            vector[idx] += 1.0f
        }
        val magnitude = sqrt(vector.sumOf { it * it.toDouble() }).toFloat()
        if (magnitude > 0) {
            for (i in vector.indices) {
                vector[i] /= magnitude
            }
        }
        return vector
    }

    private suspend fun findRelatedMemories(vector: FloatArray): List<String> {
        val allMemories = messageDao.getAllImageMemories()
        return allMemories.mapNotNull { memory ->
            val memVector = parseSemanticVector(memory.semanticVector)
            val similarity = cosineSimilarity(vector, memVector)
            if (similarity > 0.7f) memory.id else null
        }.take(5)
    }

    private fun parseSemanticVector(json: String?): FloatArray {
        if (json.isNullOrEmpty()) return FloatArray(128) { 0f }
        return try {
            Json.decodeFromString<List<Float>>(json).toFloatArray()
        } catch (e: Exception) {
            FloatArray(128) { 0f }
        }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        return if (normA > 0 && normB > 0) {
            dotProduct / (sqrt(normA.toDouble()) * sqrt(normB.toDouble())).toFloat()
        } else 0f
    }

    private fun calculateTotalStorageSize(): Long {
        return memoryDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    private fun extractMessageId(fileName: String): String {
        return fileName.substringAfter("img_mem_").substringBefore("_")
    }
}

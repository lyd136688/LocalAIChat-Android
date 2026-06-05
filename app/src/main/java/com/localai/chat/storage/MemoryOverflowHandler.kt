package com.localai.chat.storage

import android.content.Context
import android.util.Log
import com.localai.chat.data.database.ChatMessageDao
import com.localai.chat.data.database.ChatSessionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class MemoryOverflowHandler(
    private val context: Context,
    private val messageDao: ChatMessageDao,
    private val sessionDao: ChatSessionDao
) {
    companion object {
        const val TAG = "MemoryOverflowHandler"
    }

    private val memoryDir = File(context.filesDir, StorageConfig.MEMORY_DIR)

    private val _storageState = MutableStateFlow(StorageState())
    val storageState: StateFlow<StorageState> = _storageState

    suspend fun checkAndHandle() {
        val totalSize = calculateTotalStorageSize()
        val limitBytes = StorageConfig.TOTAL_STORAGE_LIMIT_GB * 1024 * 1024 * 1024
        val usagePercent = (totalSize.toDouble() / limitBytes) * 100

        _storageState.value = StorageState(
            usedBytes = totalSize,
            limitBytes = limitBytes,
            usagePercent = usagePercent,
            imageMemoryCount = memoryDir.listFiles()?.size ?: 0
        )

        Log.i(TAG, "Storage usage: ${usagePercent.toInt()}%")

        when {
            usagePercent >= 100 -> handleCritical(usagePercent)
            usagePercent >= 95 -> handleSevere(usagePercent)
            usagePercent >= StorageConfig.CLEANUP_TRIGGER_PERCENT.toDouble() -> handleModerate(usagePercent)
            usagePercent >= 70 -> handleWarning(usagePercent)
        }
    }

    private suspend fun handleWarning(usagePercent: Double) {
        Log.w(TAG, "Storage at ${usagePercent.toInt()}%, approaching limit")
        _storageState.value = _storageState.value.copy(
            level = StorageLevel.WARNING,
            message = "存储空间即将用完（${usagePercent.toInt()}%），建议清理旧对话"
        )
    }

    private suspend fun handleModerate(usagePercent: Double) {
        Log.w(TAG, "Storage at ${usagePercent.toInt()}%, starting moderate cleanup")
        val cutoffTime = System.currentTimeMillis() - StorageConfig.KEEP_RECENT_DAYS * 24 * 60 * 60 * 1000
        val deletedCount = messageDao.deleteOldMessages("all", cutoffTime)
        cleanupOrphanedFiles()
        _storageState.value = _storageState.value.copy(
            level = StorageLevel.MODERATE,
            message = "已自动清理 ${deletedCount} 条超过30天的旧消息"
        )
    }

    private suspend fun handleSevere(usagePercent: Double) {
        Log.e(TAG, "Storage at ${usagePercent.toInt()}%, severe cleanup required")
        val cutoffTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        val deletedCount = messageDao.deleteOldMessages("all", cutoffTime)
        cleanupOrphanedFiles()
        clearMediaCache()
        _storageState.value = _storageState.value.copy(
            level = StorageLevel.SEVERE,
            message = "存储空间严重不足！已清理 ${deletedCount} 条旧消息和媒体缓存"
        )
    }

    private suspend fun handleCritical(usagePercent: Double) {
        Log.e(TAG, "Storage CRITICAL at ${usagePercent.toInt()}%!")
        val allSessions = sessionDao.getAllSessions()
        val recentSessions = allSessions
            .sortedByDescending { it.updatedAt }
            .filter { it.isPinned }
            .take(StorageConfig.MIN_SESSIONS_TO_KEEP)

        var deletedCount = 0
        allSessions.forEach { session ->
            if (session !in recentSessions) {
                messageDao.deleteOldMessages(session.id, 0)
                deletedCount++
            }
        }

        cleanupOrphanedFiles()
        clearMediaCache()
        clearTempFiles()

        _storageState.value = _storageState.value.copy(
            level = StorageLevel.CRITICAL,
            message = "存储空间已满！已紧急清理，仅保留最近 ${recentSessions.size} 个会话"
        )
    }

    private suspend fun cleanupOrphanedFiles() {
        val files = memoryDir.listFiles() ?: return
        var cleaned = 0
        files.forEach { file ->
            val messageId = extractMessageId(file.name)
            val entity = messageDao.getById(messageId)
            if (entity == null || entity.fullContentPath == null) {
                file.delete()
                cleaned++
            }
        }
        if (cleaned > 0) {
            Log.i(TAG, "Cleaned $cleaned orphaned files")
        }
    }

    private fun clearMediaCache() {
        val cacheDir = context.cacheDir
        var freedBytes = 0L
        cacheDir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                file.listFiles()?.forEach { child ->
                    freedBytes += child.length()
                    child.delete()
                }
                file.delete()
            } else {
                freedBytes += file.length()
                file.delete()
            }
        }
        Log.i(TAG, "Cleared media cache: ${freedBytes / (1024*1024)}MB freed")
    }

    private fun clearTempFiles() {
        val tempDir = File(context.cacheDir, "tmp")
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
    }

    private fun calculateTotalStorageSize(): Long {
        return memoryDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    suspend fun cleanupSession(sessionId: String) {
        val messages = messageDao.getSessionMessages(sessionId)
        messages.forEach { msg ->
            if (msg.isImageMemory && msg.fullContentPath != null) {
                File(msg.fullContentPath).delete()
            }
        }
        messageDao.deleteOldMessages(sessionId, Long.MAX_VALUE)
        checkAndHandle()
    }

    private fun extractMessageId(fileName: String): String {
        return fileName.substringAfter("img_mem_").substringBefore("_")
    }
}

data class StorageState(
    val usedBytes: Long = 0,
    val limitBytes: Long = 0,
    val usagePercent: Double = 0.0,
    val imageMemoryCount: Int = 0,
    val level: StorageLevel = StorageLevel.NORMAL,
    val message: String = ""
)

enum class StorageLevel {
    NORMAL, WARNING, MODERATE, SEVERE, CRITICAL
}

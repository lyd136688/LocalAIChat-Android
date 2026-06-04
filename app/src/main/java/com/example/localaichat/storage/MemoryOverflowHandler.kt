package com.example.localaichat.storage

import android.content.Context
import android.util.Log
import com.example.localaichat.data.dao.ChatMessageDao
import com.example.localaichat.data.dao.ChatSessionDao
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

    // 存储状态
    private val _storageState = MutableStateFlow(StorageState())
    val storageState: StateFlow<StorageState> = _storageState

    /**
     * 检查存储状态 - 每次保存消息后调用
     */
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

        Log.i(TAG, "Storage usage: ${usagePercent.toInt()}% (${totalSize / (1024*1024)}MB / ${limitBytes / (1024*1024*1024)}GB)")

        when {
            usagePercent >= 100 -> handleCritical(usagePercent)
            usagePercent >= 95 -> handleSevere(usagePercent)
            usagePercent >= StorageConfig.CLEANUP_TRIGGER_PERCENT.toDouble() -> handleModerate(usagePercent)
            usagePercent >= 70 -> handleWarning(usagePercent)
        }
    }

    /**
     * 70% - 警告提醒
     */
    private suspend fun handleWarning(usagePercent: Double) {
        Log.w(TAG, "Storage at ${usagePercent.toInt()}%, approaching limit")
        _storageState.value = _storageState.value.copy(
            level = StorageLevel.WARNING,
            message = "存储空间即将用完（${usagePercent.toInt()}%），建议清理旧对话"
        )
    }

    /**
     * 80% - 中度清理：删除超过30天的非置顶会话
     */
    private suspend fun handleModerate(usagePercent: Double) {
        Log.w(TAG, "Storage at ${usagePercent.toInt()}%, starting moderate cleanup")

        val cutoffTime = System.currentTimeMillis() - StorageConfig.KEEP_RECENT_DAYS * 24 * 60 * 60 * 1000
        val deletedCount = messageDao.deleteOldMessages("all", cutoffTime)

        // 清理对应的形象记忆文件
        cleanupOrphanedFiles()

        _storageState.value = _storageState.value.copy(
            level = StorageLevel.MODERATE,
            message = "已自动清理 ${deletedCount} 条超过30天的旧消息"
        )

        Log.i(TAG, "Moderate cleanup: deleted $deletedCount old messages")
    }

    /**
     * 95% - 严重清理：只保留最近7天
     */
    private suspend fun handleSevere(usagePercent: Double) {
        Log.e(TAG, "Storage at ${usagePercent.toInt()}%, severe cleanup required")

        val cutoffTime = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        val deletedCount = messageDao.deleteOldMessages("all", cutoffTime)

        // 清理孤立文件
        cleanupOrphanedFiles()

        // 清理媒体缓存
        clearMediaCache()

        _storageState.value = _storageState.value.copy(
            level = StorageLevel.SEVERE,
            message = "存储空间严重不足！已清理 ${deletedCount} 条旧消息和媒体缓存"
        )

        Log.i(TAG, "Severe cleanup: deleted $deletedCount messages + media cache")
    }

    /**
     * 100% - 紧急清理：只保留最近3个会话
     */
    private suspend fun handleCritical(usagePercent: Double) {
        Log.e(TAG, "Storage CRITICAL at ${usagePercent.toInt()}%!")

        // 获取所有会话，按时间排序
        val allSessions = sessionDao.getAllSessions()
        val recentSessions = allSessions
            .sortedByDescending { it.updatedAt }
            .filter { it.isPinned }
            .take(StorageConfig.MIN_SESSIONS_TO_KEEP)

        // 删除不在保留列表中的会话消息
        var deletedCount = 0
        allSessions.forEach { session ->
            if (session !in recentSessions) {
                messageDao.deleteOldMessages(session.id, 0)
                deletedCount++
            }
        }

        // 清理所有非置顶的形象记忆文件
        cleanupOrphanedFiles()

        // 清理所有缓存
        clearMediaCache()
        clearTempFiles()

        _storageState.value = _storageState.value.copy(
            level = StorageLevel.CRITICAL,
            message = "存储空间已满！已紧急清理，仅保留最近 ${recentSessions.size} 个会话"
        )

        Log.e(TAG, "Critical cleanup: kept ${recentSessions.size} sessions, deleted $deletedCount")
    }

    /**
     * 清理孤立的文件（数据库中无对应记录的文件）
     */
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

    /**
     * 清理媒体缓存
     */
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

    /**
     * 清理临时文件
     */
    private fun clearTempFiles() {
        val tempDir = File(context.cacheDir, "tmp")
        if (tempDir.exists()) {
            tempDir.deleteRecursively()
        }
    }

    /**
     * 计算总存储大小
     */
    private fun calculateTotalStorageSize(): Long {
        return memoryDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * 手动清理指定会话
     */
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
    NORMAL,      // < 70%
    WARNING,     // 70-80%
    MODERATE,    // 80-95%
    SEVERE,      // 95-100%
    CRITICAL     // >= 100%
}

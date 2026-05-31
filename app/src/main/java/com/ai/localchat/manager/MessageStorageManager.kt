package com.ai.localchat.manager

import android.content.Context
import com.ai.localchat.StorageConfig
import com.ai.localchat.entity.MsgType
import com.ai.localchat.entity.Message
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader

/**
 * 核心存储管理工具类
 * 1. 自动判断消息大小，超过20MB自动转存到App私有文件
 * 2. 自动循环清理：总文件超过400GB时，删除最旧文件，保留最新1000个
 * 3. 自动读写：读消息时自动判断从数据库还是文件读取
 */
class MessageStorageManager(private val context: Context) {
    // 本地记忆私有目录，无需动态权限，其他应用无法访问
    private val memoryRootDir: File by lazy {
        File(context.filesDir, StorageConfig.MEMORY_FOLDER_NAME).apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * 保存消息内容：自动判断大小，超限转存文件
     * @return 组装好的Message实体，可直接存入Room数据库
     */
    fun buildMessageEntity(
        role: String,
        fullContent: String,
        msgType: MsgType = MsgType.TEXT
    ): Message {
        val contentLen = fullContent.length.toLong()
        // 取前5000字作为预览（和你设置的StorageConfig一致）
        val preview = fullContent.take(StorageConfig.CONTENT_PREVIEW_LENGTH)

        // 小于20MB：只存数据库，文件路径为null
        return if (contentLen < StorageConfig.DB_SAFE_THRESHOLD) {
            Message(
                role = role,
                contentPreview = preview,
                contentTotalLen = contentLen,
                msgType = msgType,
                fileAbsolutePath = null
            )
        } else {
            // 大于等于20MB：写入本地文件，数据库只存路径和预览
            val fileName = "msg_${System.currentTimeMillis()}.txt"
            val targetFile = File(memoryRootDir, fileName)

            // 写入文件，UTF-8编码，支持中文和特殊字符
            FileOutputStream(targetFile).use { output ->
                output.write(fullContent.toByteArray(Charsets.UTF_8))
            }

            // 写入后自动执行循环清理：超过400GB删除最旧文件
            autoCleanOldFiles()

            Message(
                role = role,
                contentPreview = preview,
                contentTotalLen = contentLen,
                msgType = msgType,
                fileAbsolutePath = targetFile.absolutePath
            )
        }
    }

    /**
     * 读取消息完整内容：自动判断从数据库读取，还是从本地文件读取
     */
    fun getFullContent(msg: Message): String {
        val path = msg.fileAbsolutePath ?: return msg.contentPreview
        val file = File(path)
        if (!file.exists()) return "内容已被自动清理"

        return FileReader(file).use { reader ->
            reader.readText()
        }
    }

    /**
     * 自动循环清理：当本地记忆文件总大小超过400GB时，删除最旧的文件，保留最新1000条
     */
    private fun autoCleanOldFiles() {
        val allFiles = memoryRootDir.listFiles() ?: return
        if (allFiles.isEmpty()) return

        val totalSize = allFiles.sumOf { it.length() }
        if (totalSize < StorageConfig.MAX_MEMORY_TOTAL_SIZE) return

        // 按文件修改时间排序：旧文件在前，新文件在后
        val sortedOldToNew = allFiles.sortedBy { it.lastModified() }
        // 需要删除的文件：总数量 - 保留数量
        val deleteList = sortedOldToNew.dropLast(StorageConfig.KEEP_LATEST_FILE_COUNT)

        deleteList.forEach { if (it.exists()) it.delete() }
    }

    /**
     * 手动清空所有本地记忆文件（可选功能，用于用户重置对话）
     */
    fun clearAllMemoryFiles() {
        memoryRootDir.listFiles()?.forEach { it.delete() }
    }
}


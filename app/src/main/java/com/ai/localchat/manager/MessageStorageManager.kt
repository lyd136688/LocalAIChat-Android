package com.ai.localchat.manager

import android.content.Context
import com.ai.localchat.StorageConfig
import com.ai.localchat.entity.MsgType
import com.ai.localchat.entity.Message
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader

class MessageStorageManager(private val context: Context) {
    private val memoryRootDir: File by lazy {
        File(context.filesDir, StorageConfig.MEMORY_FOLDER_NAME).apply {
            if (!exists()) mkdirs()
        }
    }

    fun buildMessageEntity(
        role: String,
        fullContent: String,
        msgType: MsgType = MsgType.TEXT
    ): Message {
        val contentLen = fullContent.length.toLong()
        val preview = fullContent.take(StorageConfig.CONTENT_PREVIEW_LENGTH)

        return if (contentLen < StorageConfig.DB_SAFE_THRESHOLD) {
            Message(
                role = role,
                contentPreview = preview,
                contentTotalLen = contentLen,
                msgType = msgType,
                fileAbsolutePath = null
            )
        } else {
            val fileName = "msg_${System.currentTimeMillis()}.txt"
            val targetFile = File(memoryRootDir, fileName)

            FileOutputStream(targetFile).use { output ->
                output.write(fullContent.toByteArray(Charsets.UTF_8))
            }
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

    fun getFullContent(msg: Message): String {
        val path = msg.fileAbsolutePath ?: return msg.contentPreview
        val file = File(path)
        if (!file.exists()) return "内容已被自动清理"
        return FileReader(file).use { reader ->
            reader.readText()
        }
    }

    private fun autoCleanOldFiles() {
        val allFiles = memoryRootDir.listFiles() ?: return
        if (allFiles.isEmpty()) return

        val totalSize = allFiles.sumOf { it.length() }
        if (totalSize < StorageConfig.MAX_MEMORY_TOTAL_SIZE) return

        val sortedOldToNew = allFiles.sortedBy { it.lastModified() }
        val deleteList = sortedOldToNew.dropLast(StorageConfig.KEEP_LATEST_FILE_COUNT)
        deleteList.forEach { if (it.exists()) it.delete() }
    }

    fun clearAllMemoryFiles() {
        memoryRootDir.listFiles()?.forEach { it.delete() }
    }
}

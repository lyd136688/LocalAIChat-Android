package com.localai.chat.storage

import android.content.Context
import java.io.File

class ImageMemoryStorage(context: Context) {
    private val storageDir = File(context.filesDir, "image_memories")
    
    init { storageDir.mkdirs() }
    
    fun saveMemory(content: String, tags: List<String>): String {
        val file = File(storageDir, "memory_" + System.currentTimeMillis() + ".txt")
        file.writeText(content)
        return file.absolutePath
    }
}

package com.localai.chat.storage

class MemoryOverflowHandler {
    fun checkAndCleanup(currentSize: Long) {
        if (currentSize > StorageConfig.MAX_STORAGE_SIZE * 0.7) {
            // cleanup logic
        }
    }
}


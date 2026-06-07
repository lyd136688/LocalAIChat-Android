package com.localai.chat.storage

class SessionMemoryManager {
    fun compressOldMessages(messages: List<String>): List<String> = messages.takeLast(10)
}

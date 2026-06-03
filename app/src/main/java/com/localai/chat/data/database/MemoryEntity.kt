package com.localai.chat.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey
    val id: Long,
    val content: String,
    val type: String,
    val timestamp: Long,
    val isArchived: Boolean = false
)


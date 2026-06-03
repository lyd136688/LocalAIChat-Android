package com.localai.chat.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val displayName: String,
    val parameters: String,
    val size: String,
    val author: String,
    val localPath: String?,
    val isDownloaded: Boolean = false,
    val downloadProgress: Int = 0
)

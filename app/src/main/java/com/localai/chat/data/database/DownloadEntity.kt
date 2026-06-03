package com.localai.chat.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    val id: String,
    val modelName: String,
    val modelId: String,
    val status: String,
    val progress: Int,
    val totalSize: Long,
    val downloadedSize: Long,
    val localPath: String,
    val timestamp: Long
)

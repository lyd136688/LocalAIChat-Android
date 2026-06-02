package com.localai.chat.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val url: String,
    val fileName: String,
    val filePath: String,
    var totalBytes: Long,
    var downloadedBytes: Long,
    var status: String,
    val modelId: String
)

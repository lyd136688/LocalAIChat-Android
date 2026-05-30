package com.ai.localchat.core.download

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile

object DownloadManager {
    private const val TAG = "DownloadManager"
    private val httpClient = OkHttpClient()

    data class DownloadTask(
        val url: String,
        val savePath: String,
        val fileName: String,
        var progress: Int = 0,
        var status: DownloadStatus = DownloadStatus.PENDING
    )

    enum class DownloadStatus {
        PENDING, DOWNLOADING, COMPLETED, FAILED
    }

    // 下载文件（支持断点续传）
    suspend fun downloadFile(
        task: DownloadTask,
        onProgress: (Int) -> Unit = {},
        onComplete: (Boolean) -> Unit = {}
    ) {
        task.status = DownloadStatus.DOWNLOADING
        onProgress(0)

        try {
            val file = File(task.savePath, task.fileName)
            val downloadedSize = if (file.exists()) file.length() else 0

            val request = Request.Builder()
                .url(task.url)
                .header("Range", "bytes=$downloadedSize-")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful && response.code != 206) {
                task.status = DownloadStatus.FAILED
                onComplete(false)
                return
            }

            val totalSize = response.body?.contentLength() ?: 0
            val inputStream = response.body?.byteStream() ?: return
            val randomAccessFile = RandomAccessFile(file, "rw")
            randomAccessFile.seek(downloadedSize)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = downloadedSize

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                randomAccessFile.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                val progress = (totalRead * 100 / (downloadedSize + totalSize)).toInt()
                task.progress = progress
                onProgress(progress)
            }

            randomAccessFile.close()
            inputStream.close()

            task.status = DownloadStatus.COMPLETED
            onProgress(100)
            onComplete(true)
        } catch (e: Exception) {
            Log.e(TAG, "下载失败", e)
            task.status = DownloadStatus.FAILED
            onComplete(false)
        }
    }
}


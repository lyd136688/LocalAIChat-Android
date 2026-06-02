package com.localai.chat

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.CoroutineContext

data class DownloadTask(
    val url: String,
    val fileName: String,
    val modelId: String,
    var status: DownloadStatus = DownloadStatus.PENDING,
    var progress: Int = 0,
    var downloadedBytes: Long = 0L,
    var totalBytes: Long = 0L,
    var error: String? = null
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED
}

class DownloadManager(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val _downloadTasks = MutableStateFlow<Map<String, DownloadTask>>(emptyMap())
    val downloadTasks: StateFlow<Map<String, DownloadTask>> = _downloadTasks.asStateFlow()

    private var activeJobs = mutableMapOf<String, Job>()

    fun startDownload(url: String, fileName: String, modelId: String) {
        if (activeJobs.containsKey(url)) return

        val task = DownloadTask(url, fileName, modelId, DownloadStatus.DOWNLOADING)
        _downloadTasks.value = _downloadTasks.value.toMutableMap().apply { put(url, task) }

        val job = CoroutineScope(Dispatchers.IO).launch {
            downloadFile(url, fileName)
        }
        activeJobs[url] = job
    }

    fun pauseDownload(url: String) {
        activeJobs[url]?.cancel()
        activeJobs.remove(url)
        _downloadTasks.value = _downloadTasks.value.toMutableMap().apply {
            get(url)?.let { task ->
                put(url, task.copy(status = DownloadStatus.PAUSED))
            }
        }
    }

    fun resumeDownload(url: String) {
        val currentTask = _downloadTasks.value[url]
        if (currentTask != null && currentTask.status == DownloadStatus.PAUSED) {
            startDownload(url, currentTask.fileName, currentTask.modelId)
        }
    }

    private suspend fun downloadFile(url: String, fileName: String) {
        val file = File(context.getExternalFilesDir(null), "models/$fileName")
        file.parentFile?.mkdirs()

        var pendingOffset = 0L
        var pendingTask: DownloadTask? = null

        withContext(Dispatchers.Main) {
            pendingTask = _downloadTasks.value[url]
            pendingTask?.let { task ->
                pendingOffset = task.downloadedBytes
                _downloadTasks.value = _downloadTasks.value.toMutableMap().apply {
                    put(url, task.copy(status = DownloadStatus.DOWNLOADING))
                }
            }
        }

        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=$pendingOffset-")
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val body = response.body ?: throw IOException("Response body is null")
            val contentLength = body.contentLength()

            withContext(Dispatchers.Main) {
                val currentTask = _downloadTasks.value[url]
                currentTask?.let { task ->
                    _downloadTasks.value = _downloadTasks.value.toMutableMap().apply {
                        put(url, task.copy(totalBytes = task.downloadedBytes + contentLength))
                    }
                }
            }

            val fileOutputStream = FileOutputStream(file, true)
            val inputStream = body.byteStream()
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = pendingOffset

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                fileOutputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                withContext(Dispatchers.Main) {
                    val currentTask = _downloadTasks.value[url]
                    currentTask?.let { task ->
                        val progress = ((totalBytesRead.toDouble() / (task.totalBytes.coerceAtLeast(1))) * 100).toInt()
                        _downloadTasks.value = _downloadTasks.value.toMutableMap().apply {
                            put(url, task.copy(progress = progress, downloadedBytes = totalBytesRead))
                        }
                    }
                }
            }

            fileOutputStream.close()
            inputStream.close()

            withContext(Dispatchers.Main) {
                val currentTask = _downloadTasks.value[url]
                currentTask?.let { task ->
                    _downloadTasks.value = _downloadTasks.value.toMutableMap().apply {
                        put(url, task.copy(status = DownloadStatus.COMPLETED, progress = 100))
                    }
                }
            }
            activeJobs.remove(url)

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                val currentTask = _downloadTasks.value[url]
                currentTask?.let { task ->
                    _downloadTasks.value = _downloadTasks.value.toMutableMap().apply {
                        put(url, task.copy(status = DownloadStatus.FAILED, error = e.message))
                    }
                }
            }
            activeJobs.remove(url)
            throw e
        }
    }
}

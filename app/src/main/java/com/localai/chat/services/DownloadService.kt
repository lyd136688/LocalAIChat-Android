package com.localai.chat.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.localai.chat.MainActivity
import com.localai.chat.MyApplication
import com.localai.chat.R
import com.localai.chat.utils.InferenceScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : Service() {
    
    companion object {
        const val ACTION_PAUSE = "pause"
        const val ACTION_RESUME = "resume"
        const val ACTION_CANCEL = "cancel"
        private const val NOTIFICATION_ID = 1001
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var downloadJob: Job? = null
    private var isPaused = false
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val downloadId = intent?.getStringExtra("download_id")
        
        when (action) {
            ACTION_PAUSE -> {
                isPaused = true
                downloadJob?.cancel()
                updateNotification("下载已暂停", 0)
            }
            ACTION_RESUME -> {
                isPaused = false
                downloadId?.let { startDownload(it) }
            }
            ACTION_CANCEL -> {
                downloadJob?.cancel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                val url = intent?.getStringExtra("download_url") ?: ""
                val savePath = intent?.getStringExtra("save_path") ?: ""
                val modelName = intent?.getStringExtra("model_name") ?: "模型"
                
                startForeground(NOTIFICATION_ID, createNotification(modelName, 0))
                downloadId?.let { startDownload(it, url, savePath, modelName) }
            }
        }
        
        return START_STICKY
    }
    
    private fun startDownload(
        downloadId: String,
        downloadUrl: String = "",
        savePath: String = "",
        modelName: String = ""
    ) {
        downloadJob = serviceScope.launch {
            try {
                val entity = MyApplication.instance.database.downloadDao().getDownloadById(downloadId)
                    ?: return@launch
                
                val url = URL(downloadUrl.ifEmpty { entity.localPath })
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 60000
                
                val file = File(savePath.ifEmpty { entity.localPath })
                file.parentFile?.mkdirs()
                
                // 支持断点续传
                val downloadedSize = if (file.exists()) file.length() else 0
                if (downloadedSize > 0) {
                    connection.setRequestProperty("Range", "bytes=$downloadedSize-")
                }
                
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(file, downloadedSize > 0)
                
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead = downloadedSize
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (isPaused) break
                    
                    outputStream.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    
                    val progress = if (entity.totalSize > 0) {
                        ((totalRead * 100) / entity.totalSize).toInt()
                    } else 0
                    
                    updateNotification("正在下载: $modelName", progress)
                    
                    val updated = entity.copy(
                        status = "DOWNLOADING",
                        progress = progress,
                        downloadedSize = totalRead
                    )
                    MyApplication.instance.database.downloadDao().updateDownload(updated)
                }
                
                outputStream.close()
                inputStream.close()
                
                if (!isPaused) {
                    // 下载完成
                    val completed = entity.copy(
                        status = "COMPLETED",
                        progress = 100,
                        downloadedSize = entity.totalSize
                    )
                    MyApplication.instance.database.downloadDao().updateDownload(completed)
                    updateNotification("下载完成: $modelName", 100)
                    
                    // 自动加载模型（如果是GGUF文件）
                    if (file.extension.lowercase() == "gguf") {
                        loadModelAfterDownload(file.absolutePath, modelName)
                    }
                }
                
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                
            } catch (e: Exception) {
                e.printStackTrace()
                updateNotification("下载失败: ${e.message}", 0)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }
    
    /**
     * 下载完成后自动加载模型
     */
    private fun loadModelAfterDownload(modelPath: String, modelName: String) {
        serviceScope.launch(Dispatchers.Main) {
            try {
                val scheduler = InferenceScheduler(applicationContext)
                val loaded = scheduler.loadLocalModel(modelPath)
                
                if (loaded) {
                    updateNotification("模型已加载: $modelName", 100)
                    
                    // 发送广播通知UI更新
                    sendBroadcast(Intent(ACTION_MODEL_LOADED).apply {
                        putExtra("model_path", modelPath)
                        putExtra("model_name", modelName)
                    })
                } else {
                    updateNotification("模型加载失败: $modelName", 100)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun createNotification(title: String, progress: Int): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, "download_channel")
            .setContentTitle(title)
            .setContentText("$progress%")
            .setSmallIcon(R.drawable.ic_download)
            .setProgress(100, progress, progress == 0)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(title: String, progress: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(title, progress))
    }
    
    companion object {
        const val ACTION_MODEL_LOADED = "com.localai.chat.MODEL_LOADED"
    }
}


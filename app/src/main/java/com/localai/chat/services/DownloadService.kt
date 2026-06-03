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
import com.localai.chat.MyApplication
import com.localai.chat.R
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
                downloadId?.let { startDownload(it) }
            }
        }
        
        return START_STICKY
    }
    
    private fun startDownload(downloadId: String) {
        downloadJob = serviceScope.launch {
            try {
                val entity = MyApplication.instance.database.downloadDao().getDownloadById(downloadId) ?: return@launch
                
                val url = URL(entity.localPath)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 30000
                connection.readTimeout = 60000
                
                val file = File(entity.localPath)
                file.parentFile?.mkdirs()
                
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(file, entity.downloadedSize > 0)
                
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead = entity.downloadedSize
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (isPaused) break
                    
                    outputStream.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    
                    val progress = ((totalRead * 100) / entity.totalSize).toInt()
                    updateNotification("正在下载: ${entity.modelName}", progress)
                    
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
                    val completed = entity.copy(
                        status = "COMPLETED",
                        progress = 100,
                        downloadedSize = entity.totalSize
                    )
                    MyApplication.instance.database.downloadDao().updateDownload(completed)
                    updateNotification("下载完成: ${entity.modelName}", 100)
                }
                
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                
            } catch (e: Exception) {
                e.printStackTrace()
                updateNotification("下载失败", 0)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }
    
    private fun createNotification(title: String, progress: Int): Notification {
        val intent = Intent(this, com.localai.chat.MainActivity::class.java)
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
}

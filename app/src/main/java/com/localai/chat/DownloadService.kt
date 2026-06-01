package com.localai.chat

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadService : Service() {
    private val client = OkHttpClient()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY
        val destFile = File(getExternalFilesDir(null), "models/${url.substringAfterLast("/")}")
        startForeground(1, createNotification())
        downloadFile(url, destFile)
        return START_NOT_STICKY
    }

    private fun downloadFile(url: String, dest: File) {
        Thread {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                response.body?.let { body ->
                    dest.parentFile?.mkdirs()
                    FileOutputStream(dest).use { output ->
                        body.byteStream().copyTo(output)
                    }
                }
                stopSelf()
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
            }
        }.start()
    }

    private fun createNotification(): Notification {
        val channelId = "download_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "模型下载", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("下载中")
            .setContentText("正在下载模型文件")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

package com.localai.chat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.localai.chat.data.database.AppDatabase
import com.localai.chat.utils.DownloadManager
import com.localai.chat.utils.HardwareDetector
import com.localai.chat.utils.MemoryManager

class MyApplication : Application() {
    
    companion object {
        lateinit var instance: MyApplication
            private set
    }
    
    lateinit var database: AppDatabase
        private set
    lateinit var downloadManager: DownloadManager
        private set
    lateinit var memoryManager: MemoryManager
        private set
    lateinit var hardwareDetector: HardwareDetector
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        database = AppDatabase.getDatabase(this)
        downloadManager = DownloadManager(this)
        memoryManager = MemoryManager(this)
        hardwareDetector = HardwareDetector(this)
        
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadChannel = NotificationChannel(
                "download_channel",
                "模型下载",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示模型下载进度"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(downloadChannel)
        }
    }
}


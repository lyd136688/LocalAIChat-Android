package com.localai.chat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.localai.chat.data.database.AppDatabase
import com.localai.chat.native.LlamaHelper
import com.localai.chat.storage.ImageMemoryStorage
import com.localai.chat.storage.MemoryOverflowHandler
import com.localai.chat.storage.SessionMemoryManager
import com.localai.chat.utils.DownloadManager
import com.localai.chat.utils.HardwareDetector
import com.localai.chat.utils.MemoryManager

class MyApplication : Application() {

    lateinit var database: AppDatabase
    lateinit var downloadManager: DownloadManager
    lateinit var memoryManager: MemoryManager
    lateinit var hardwareDetector: HardwareDetector
    lateinit var llamaHelper: LlamaHelper
    lateinit var imageMemoryStorage: ImageMemoryStorage
    lateinit var sessionMemoryManager: SessionMemoryManager
    lateinit var memoryOverflowHandler: MemoryOverflowHandler

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        hardwareDetector = HardwareDetector(this)
        downloadManager = DownloadManager(this)
        memoryManager = MemoryManager(this, database)
        llamaHelper = LlamaHelper(this)
        imageMemoryStorage = ImageMemoryStorage(this, database.chatMessageDao())
        sessionMemoryManager = SessionMemoryManager()
        memoryOverflowHandler = MemoryOverflowHandler(
            this,
            database.chatMessageDao(),
            database.chatSessionDao()
        )
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val downloadChannel = NotificationChannel(
                "download_channel",
                "模型下载",
                NotificationManager.IMPORTANCE_LOW
            )
            downloadChannel.description = "模型下载进度通知"

            val inferenceChannel = NotificationChannel(
                "inference_channel",
                "模型推理",
                NotificationManager.IMPORTANCE_LOW
            )
            inferenceChannel.description = "模型推理状态通知"

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(listOf(downloadChannel, inferenceChannel))
        }
    }
}

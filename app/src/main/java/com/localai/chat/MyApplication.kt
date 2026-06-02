package com.localai.chat

import android.app.Application

class MyApplication : Application() {
    val downloadManager: DownloadManager by lazy {
        DownloadManager(applicationContext)
    }
}

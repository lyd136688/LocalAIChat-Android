package com.ai.localchat

import android.app.Application
import android.database.CursorWindow

class LocalAIChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 设置CursorWindow为32MB，解决数据库单行过大崩溃
        try {
            val field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 32 * 1024 * 1024)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


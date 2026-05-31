package com.ai.localchat // 务必改成你项目真实包名

import android.app.Application
import android.database.CursorWindow

class LocalAIChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 专属本机：设置 CursorWindow = 24MB
        try {
            val field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            // 24 * 1024 * 1024 = 24MB
            field.set(null, 24 * 1024 * 1024)
        } catch (e: Exception) {
            // 低版本设备自动降级，不崩溃
            e.printStackTrace()
        }
    }
}


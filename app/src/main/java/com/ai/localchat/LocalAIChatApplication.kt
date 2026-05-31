package com.ai.localchat

import android.app.Application
import android.database.CursorWindow

class LocalAIChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 专属你的Redmi K70 Ultra：设置CursorWindow为24MB，彻底解决Row too big崩溃
        try {
            val field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 24 * 1024 * 1024) // 24MB，适配Android 14+和24GB内存
        } catch (e: Exception) {
            // 低版本设备自动降级，不影响运行
            e.printStackTrace()
        }
    }
}


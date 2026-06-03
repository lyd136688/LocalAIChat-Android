package com.localai.chat.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build

class HardwareDetector(private val context: Context) {
    
    fun getTotalRAMGB(): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamBytes = memoryInfo.totalMem
        return (totalRamBytes / (1024.0 * 1024.0 * 1024.0)).toInt()
    }
    
    fun getAvailableRAMMB(): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / (1024 * 1024)
    }
    
    fun getCPUInfo(): String {
        return try {
            val cores = Runtime.getRuntime().availableProcessors()
            val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
            "$cores核 | $abi"
        } catch (e: Exception) {
            "未知"
        }
    }
    
    fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} | Android ${Build.VERSION.RELEASE} | ${getCPUInfo()} | ${getTotalRAMGB()}GB RAM"
    }
    
    fun getRecommendedModelSize(): String {
        val ramGB = getTotalRAMGB()
        return when {
            ramGB >= 16 -> "13B"
            ramGB >= 8 -> "7B"
            ramGB >= 6 -> "3B"
            else -> "1.5B"
        }
    }
    
    fun getRecommendedModels(): List<String> {
        return when (getRecommendedModelSize()) {
            "13B" -> listOf("LLaMA-2-13B-Chat", "Qwen-14B-Chat")
            "7B" -> listOf("LLaMA-2-7B-Chat", "Qwen-7B-Chat", "ChatGLM3-6B")
            "3B" -> listOf("Qwen-1.5-4B-Chat", "TinyLlama-1.1B")
            else -> listOf("TinyLlama-1.1B", "Qwen-1.5-0.5B-Chat")
        }
    }
    
    fun canRunModel(modelSizeGB: Double): Boolean {
        val availableRAMMB = getAvailableRAMMB()
        val requiredMB = modelSizeGB * 1024 * 1.5
        return availableRAMMB >= requiredMB
    }
}

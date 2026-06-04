package com.example.localaichat.native

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.localaichat.storage.StorageConfig

object DeviceProfileDetector {
    private const val TAG = "DeviceProfileDetector"

    fun detect(context: Context): DeviceProfile {
        val totalMemoryMB = getTotalMemoryMB(context)
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val board = Build.BOARD.lowercase()
        val hardware = Build.HARDWARE.lowercase()

        Log.i(TAG, "Device: board=$board, hardware=$hardware, memory=${totalMemoryMB}MB, cores=$cpuCores")

        return when {
            // 天玑9300+ 检测
            isDimensity9300(board, hardware) -> {
                Log.i(TAG, "Detected MediaTek Dimensity 9300+")
                DeviceProfile(
                    name = "Dimensity 9300+",
                    nCtx = StorageConfig.KV_CACHE_DIMENSITY_CTX,
                    nBatch = 1024,
                    nThreads = 8,
                    gpuLayers = 33,
                    maxSessionMessages = 200,
                    maxSessionTokens = 8192,
                    localStorageLimitMB = 2048,
                    useNpu = true,
                    useGpu = true
                )
            }

            // 高端设备：8GB+ 内存
            totalMemoryMB >= 8192 -> {
                Log.i(TAG, "Detected high-end device")
                DeviceProfile(
                    name = "High-End",
                    nCtx = StorageConfig.KV_CACHE_HIGH_CTX,
                    nBatch = 512,
                    nThreads = 8,
                    gpuLayers = 20,
                    maxSessionMessages = 100,
                    maxSessionTokens = 4096,
                    localStorageLimitMB = 1024,
                    useNpu = false,
                    useGpu = true
                )
            }

            // 中端设备：6GB+ 内存
            totalMemoryMB >= 6144 -> {
                Log.i(TAG, "Detected mid-range device")
                DeviceProfile(
                    name = "Mid-Range",
                    nCtx = StorageConfig.KV_CACHE_MID_CTX,
                    nBatch = 512,
                    nThreads = 6,
                    gpuLayers = 10,
                    maxSessionMessages = 50,
                    maxSessionTokens = 2048,
                    localStorageLimitMB = 500,
                    useNpu = false,
                    useGpu = true
                )
            }

            // 低端设备
            else -> {
                Log.i(TAG, "Detected low-end device")
                DeviceProfile(
                    name = "Low-End",
                    nCtx = StorageConfig.KV_CACHE_LOW_CTX,
                    nBatch = 256,
                    nThreads = 4,
                    gpuLayers = 0,
                    maxSessionMessages = 20,
                    maxSessionTokens = 1024,
                    localStorageLimitMB = 200,
                    useNpu = false,
                    useGpu = false
                )
            }
        }
    }

    private fun isDimensity9300(board: String, hardware: String): Boolean {
        return board.contains("mt6989", ignoreCase = true) ||
               board.contains("mt6983", ignoreCase = true) ||
               hardware.contains("dimensity", ignoreCase = true) ||
               hardware.contains("mt6989", ignoreCase = true)
    }

    private fun getTotalMemoryMB(context: Context): Long {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return am.memoryClass * 1024L // memoryClass 返回 MB
    }
}

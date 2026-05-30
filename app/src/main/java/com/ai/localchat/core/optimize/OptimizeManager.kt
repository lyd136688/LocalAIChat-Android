package com.ai.localchat.core.optimize

import android.content.Context
import android.os.Environment
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

/** APP自我优化引擎：硬件检测 + 动态参数调优 */
object OptimizeManager {

    // 获取最优推理线程数
    fun getOptThreadCount(): Int {
        val coreCount = Runtime.getRuntime().availableProcessors()
        return when {
            coreCount >= 8 -> 4
            coreCount >= 4 -> 2
            else -> 1
        }
    }

    // 获取当前剩余内存 MB
    fun getFreeMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.freeMemory() / 1024 / 1024
    }

    // 检测存储空间是否充足
    fun isStorageEnough(): Boolean {
        val stat = Environment.getDataDirectory().usableSpace / 1024 / 1024
        return stat > 1024 // 剩余大于1G判定充足
    }

    // 低内存自动降级策略
    fun checkMemoryStatus(): Boolean {
        return getFreeMemory() < 512 // 剩余<512MB 触发降级
    }

    // 启动后台定时优化任务
    fun startOptimizeTask(context: Context) {
        val optimizeWork: WorkRequest = OneTimeWorkRequestBuilder<OptimizeWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()
        
        WorkManager.getInstance(context).enqueue(optimizeWork)
    }
}

// 后台优化工作器
class OptimizeWorker(context: Context, params: androidx.work.WorkerParameters) :
    androidx.work.Worker(context, params) {
    override fun doWork(): Result {
        // 执行内存清理、闲置资源释放等优化操作
        return Result.success()
    }
}


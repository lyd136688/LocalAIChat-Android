package com.localai.chat.utils

import android.content.Context
import android.content.Intent
import com.localai.chat.data.database.DownloadEntity
import com.localai.chat.data.models.DownloadStatus
import com.localai.chat.data.models.DownloadTask
import com.localai.chat.services.DownloadService
import kotlinx.coroutines.flow.Flow

class DownloadManager(private val context: Context) {
    
    private val database = com.localai.chat.MyApplication.instance.database
    
    fun getAllDownloads(): Flow<List<DownloadEntity>> {
        return database.downloadDao().getAllDownloads()
    }
    
    suspend fun startDownload(task: DownloadTask) {
        val entity = DownloadEntity(
            id = task.id,
            modelName = task.modelName,
            modelId = task.modelId,
            status = DownloadStatus.PENDING.name,
            progress = 0,
            totalSize = task.totalSize,
            downloadedSize = 0,
            localPath = task.savePath,
            timestamp = System.currentTimeMillis()
        )
        database.downloadDao().insertDownload(entity)
        
        val intent = Intent(context, DownloadService::class.java).apply {
            putExtra("download_id", task.id)
            putExtra("download_url", task.downloadUrl)
            putExtra("save_path", task.savePath)
            putExtra("model_name", task.modelName)
        }
        context.startForegroundService(intent)
    }
    
    suspend fun pauseDownload(downloadId: String) {
        val entity = database.downloadDao().getDownloadById(downloadId) ?: return
        val updated = entity.copy(status = DownloadStatus.PAUSED.name)
        database.downloadDao().updateDownload(updated)
        
        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_PAUSE
            putExtra("download_id", downloadId)
        }
        context.startService(intent)
    }
    
    suspend fun resumeDownload(downloadId: String) {
        val entity = database.downloadDao().getDownloadById(downloadId) ?: return
        val updated = entity.copy(status = DownloadStatus.DOWNLOADING.name)
        database.downloadDao().updateDownload(updated)
        
        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_RESUME
            putExtra("download_id", downloadId)
        }
        context.startService(intent)
    }
    
    suspend fun cancelDownload(downloadId: String) {
        database.downloadDao().deleteDownload(downloadId)
        
        val intent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_CANCEL
            putExtra("download_id", downloadId)
        }
        context.startService(intent)
    }
}

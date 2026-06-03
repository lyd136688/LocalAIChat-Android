package com.localai.chat.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelDao {
    
    @Query("SELECT * FROM models ORDER BY name ASC")
    fun getAllModels(): Flow<List<ModelEntity>>
    
    @Query("SELECT * FROM models WHERE isDownloaded = 1 ORDER BY name ASC")
    fun getDownloadedModels(): Flow<List<ModelEntity>>
    
    @Query("SELECT * FROM models WHERE id = :id")
    suspend fun getModelById(id: String): ModelEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: ModelEntity)
    
    @Update
    suspend fun updateModel(model: ModelEntity)
    
    @Query("UPDATE models SET isDownloaded = 1, localPath = :path WHERE id = :id")
    suspend fun markAsDownloaded(id: String, path: String)
    
    @Query("DELETE FROM models WHERE id = :id")
    suspend fun deleteModel(id: String)
}

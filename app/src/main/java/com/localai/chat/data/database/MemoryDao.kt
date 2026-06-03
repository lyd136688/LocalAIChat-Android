package com.localai.chat.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    
    @Query("SELECT * FROM memories WHERE type = :type AND isArchived = 0 ORDER BY timestamp DESC")
    fun getMemoriesByType(type: String): Flow<List<MemoryEntity>>
    
    @Query("SELECT * FROM memories WHERE isArchived = 1 ORDER BY timestamp DESC")
    fun getArchivedMemories(): Flow<List<MemoryEntity>>
    
    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Long): MemoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)
    
    @Update
    suspend fun updateMemory(memory: MemoryEntity)
    
    @Query("UPDATE memories SET isArchived = 1 WHERE id = :id")
    suspend fun archiveMemory(id: Long)
    
    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)
    
    @Query("DELETE FROM memories")
    suspend fun deleteAllMemories()
}

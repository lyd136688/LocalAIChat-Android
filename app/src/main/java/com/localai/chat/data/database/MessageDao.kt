package com.localai.chat.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE type = :type ORDER BY createdAt DESC")
    fun getByType(type: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE content LIKE :query")
    fun search(query: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getById(id: String): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity)

    @Update
    suspend fun update(memory: MemoryEntity)

    @Delete
    suspend fun delete(memory: MemoryEntity)

    @Query("UPDATE memories SET type = 'long_term', archivedAt = :timestamp WHERE id = :id")
    suspend fun archiveToLongTerm(id: String, timestamp: Long)

    @Query("DELETE FROM memories WHERE createdAt < :beforeTime AND type = 'short_term'")
    suspend fun deleteOldMemories(beforeTime: Long): Int

    @Query("SELECT * FROM memories WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun getByTag(tag: String): Flow<List<MemoryEntity>>

    @Query("UPDATE memories SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: String, tags: String)
}


package com.localai.chat.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ChatSessionDao {
    @Insert
    suspend fun insert(session: ChatSessionEntity)
    
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    suspend fun getAll(): List<ChatSessionEntity>
    
    @Update
    suspend fun update(session: ChatSessionEntity)
    
    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun delete(id: String)
}

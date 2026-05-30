package com.ai.localchat.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ai.localchat.data.entity.ChatRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun insertChat(chat: ChatRecord)

    @Query("SELECT * FROM chat_record ORDER BY createTime DESC")
    fun getAllChat(): Flow<List<ChatRecord>>

    @Query("DELETE FROM chat_record WHERE id = :id")
    suspend fun deleteChat(id: Long)

    @Query("DELETE FROM chat_record")
    suspend fun clearAllChat()
}

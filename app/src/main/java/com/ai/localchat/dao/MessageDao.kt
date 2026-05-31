package com.ai.localchat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ai.localchat.entity.Message

@Dao
interface MessageDao {

    @Insert
    suspend fun insertMsg(msg: Message): Long

    @Update
    suspend fun updateMsg(msg: Message)

    @Query("SELECT * FROM chat_messages ORDER BY timeStamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMsgByPage(limit: Int, offset: Int): List<Message>

    @Query("SELECT * FROM chat_messages WHERE id = :msgId")
    suspend fun getMsgById(msgId: Long): Message?

    @Delete
    suspend fun deleteMsg(msg: Message)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMsg()
}


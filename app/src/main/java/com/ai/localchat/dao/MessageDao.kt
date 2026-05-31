package com.ai.localchat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ai.localchat.entity.Message

@Dao
interface MessageDao {

    // 插入单条消息，返回数据库自增ID
    @Insert
    suspend fun insertMsg(msg: Message): Long

    // 更新已有消息
    @Update
    suspend fun updateMsg(msg: Message)

    // 分页加载聊天记录（按时间倒序，最新消息在前）
    @Query("SELECT * FROM chat_messages ORDER BY timeStamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getMsgByPage(limit: Int, offset: Int): List<Message>

    // 根据消息ID查询单条消息
    @Query("SELECT * FROM chat_messages WHERE id = :msgId")
    suspend fun getMsgById(msgId: Long): Message?

    // 删除单条消息
    @Delete
    suspend fun deleteMsg(msg: Message)

    // 清空全部聊天记录
    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMsg()
}


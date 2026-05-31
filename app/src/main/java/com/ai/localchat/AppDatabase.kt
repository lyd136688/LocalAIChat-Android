package com.ai.localchat

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.ai.localchat.dao.MessageDao
import com.ai.localchat.entity.Message

// 数据库版本：后续修改表结构时递增版本号
@Database(entities = [Message::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // 绑定消息DAO
    abstract fun messageDao(): MessageDao

    companion object {
        // 单例模式，全局唯一数据库实例
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_database" // 数据库文件名
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


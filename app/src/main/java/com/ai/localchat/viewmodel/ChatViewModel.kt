package com.ai.localchat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ai.localchat.AppDatabase
import com.ai.localchat.entity.Message
import com.ai.localchat.entity.MsgType
import com.ai.localchat.manager.MessageStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    // 初始化数据库实例、DAO、存储管理工具
    private val db = AppDatabase.getInstance(application)
    private val messageDao = db.messageDao()
    private val storageManager = MessageStorageManager(application)

    // 聊天列表数据流，供UI页面监听并自动刷新
    private val _chatMsgList = MutableStateFlow<List<Message>>(emptyList())
    val chatMsgList: StateFlow<List<Message>> = _chatMsgList

    /**
     * 新增消息（用户发送消息 / AI 回复消息时调用）
     * 自动判断内容大小：≤20MB存数据库，＞20MB自动转本地文件
     */
    fun addNewMessage(role: String, content: String, msgType: MsgType = MsgType.TEXT) {
        viewModelScope.launch {
            val newMessage = storageManager.buildMessageEntity(role, content, msgType)
            messageDao.insertMsg(newMessage)
            loadMessageList(page = 0)
        }
    }

    /**
     * 分页加载聊天记录（上拉加载更多历史记录）
     * @param page 页码，从 0 开始
     * @param pageSize 单页加载条数，默认20条
     */
    fun loadMessageList(page: Int, pageSize: Int = 20) {
        viewModelScope.launch {
            val offset = page * pageSize
            val list = messageDao.getMsgByPage(pageSize, offset)
            if (page == 0) {
                _chatMsgList.value = list
            } else {
                _chatMsgList.value = _chatMsgList.value + list
            }
        }
    }

    /**
     * 获取单条消息完整内容（自动适配：读数据库 / 读本地文件）
     */
    fun getFullMessageContent(msg: Message): String {
        return storageManager.getFullContent(msg)
    }

    /**
     * 删除单条聊天记录
     */
    fun deleteSingleMessage(msg: Message) {
        viewModelScope.launch {
            messageDao.deleteMsg(msg)
            loadMessageList(page = 0)
        }
    }

    /**
     * 一键清空全部聊天记录 + 清空本地记忆文件
     */
    fun clearAllChat() {
        viewModelScope.launch {
            messageDao.clearAllMsg()
            storageManager.clearAllMemoryFiles()
            _chatMsgList.value = emptyList()
        }
    }
}


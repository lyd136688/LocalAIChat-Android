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
    private val db = AppDatabase.getInstance(application)
    private val messageDao = db.messageDao()
    private val storageManager = MessageStorageManager(application)

    private val _chatMsgList = MutableStateFlow<List<Message>>(emptyList())
    val chatMsgList: StateFlow<List<Message>> = _chatMsgList

    fun addNewMessage(role: String, content: String, msgType: MsgType = MsgType.TEXT) {
        viewModelScope.launch {
            val newMessage = storageManager.buildMessageEntity(role, content, msgType)
            messageDao.insertMsg(newMessage)
            loadMessageList(page = 0)
        }
    }

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

    fun getFullMessageContent(msg: Message): String {
        return storageManager.getFullContent(msg)
    }

    fun deleteSingleMessage(msg: Message) {
        viewModelScope.launch {
            messageDao.deleteMsg(msg)
            loadMessageList(page = 0)
        }
    }

    fun clearAllChat() {
        viewModelScope.launch {
            messageDao.clearAllMsg()
            storageManager.clearAllMemoryFiles()
            _chatMsgList.value = emptyList()
        }
    }
}


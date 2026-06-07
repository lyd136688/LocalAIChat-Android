package com.localai.chat.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localai.chat.native.InferenceScheduler
import com.localai.chat.storage.SessionMemoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val inferenceScheduler: InferenceScheduler,
    private val sessionManager: SessionMemoryManager
) : ViewModel() {

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage("欢迎使用 LocalAI Chat！我是本地运行的AI助手，所有对话都在你的设备上处理，保护隐私安全。", false),
            ChatMessage("你可以直接输入问题开始对话。所有设置都可以在菜单中调整。", false)
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun sendMessage(text: String) {
        val userMessage = ChatMessage(text, true)
        _messages.value = listOf(userMessage) + _messages.value

        viewModelScope.launch {
            try {
                val response = generateMockResponse(text)
                val aiMessage = ChatMessage(response, false)
                _messages.value = listOf(aiMessage) + _messages.value
            } catch (e: Exception) {
                val errorMessage = ChatMessage("抱歉，生成响应时出错了：${e.message}", false)
                _messages.value = listOf(errorMessage) + _messages.value
            }
        }
    }

    private suspend fun generateMockResponse(userInput: String): String {
        // 模拟 AI 响应（实际应用中调用 inferenceScheduler.generate）
        return when {
            userInput.contains("你好") || userInput.contains("hello", true) ->
                "你好！很高兴见到你。我是你的本地AI助手，所有对话都在你的设备上处理，确保隐私安全。有什么我可以帮助你的吗？"
            userInput.contains("模型") || userInput.contains("model", true) ->
                "当前使用的是 llama.cpp 本地推理引擎。模型配置包括：nCtx=4096, nBatch=512, nThreads=8（根据设备性能自动检测）。"
            userInput.contains("记忆") || userInput.contains("memory", true) ->
                "系统采用分层记忆管理：短期记忆（当前会话上下文）、长期记忆（形象记忆）和云端同步。默认20MB安全阈值，超过会自动转移到长期记忆。"
            userInput.contains("天玑") || userInput.contains("9300") ->
                "检测到天玑9300+处理器，已自动启用优化配置：8线程、大核优先、推荐 Q4_K_M 量化模型，推理速度最优。"
            userInput.contains("帮助") || userInput.contains("help", true) ->
                "可用功能：1）自然语言对话 2）上下文记忆 3）设备优化配置 4）隐私本地处理。如需更多帮助请随时提问。"
            else -> {
                val responses = listOf(
                    "这是一个有趣的问题。让我分析一下：关于「$userInput」，我的理解是...",
                    "好的，我来帮你处理「$userInput」。以下是我的建议：",
                    "收到你的消息「$userInput」。根据本地模型的分析...",
                    "关于「$userInput」，我有以下想法可供参考..."
                )
                responses.random()
            }
        }
    }
}

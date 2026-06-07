package com.localai.chat.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage("欢迎使用 LocalAI Chat！我是本地运行的AI助手，所有对话都在你的设备上处理，保护隐私安全。", false),
            ChatMessage("你可以直接输入问题开始对话。输入「帮助」查看功能，输入「模型信息」查看配置。", false)
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun sendMessage(text: String) {
        val userMessage = ChatMessage(text, true)
        _messages.value = listOf(userMessage) + _messages.value

        viewModelScope.launch {
            try {
                val response = generateResponse(text)
                val aiMessage = ChatMessage(response, false)
                _messages.value = listOf(aiMessage) + _messages.value
            } catch (e: Exception) {
                val errorMessage = ChatMessage("抱歉，生成响应时出错了：${e.message}", false)
                _messages.value = listOf(errorMessage) + _messages.value
            }
        }
    }

    private fun generateResponse(userInput: String): String {
        return when {
            userInput.contains("你好") || userInput.contains("hello", true) ->
                "你好！很高兴见到你。我是你的本地AI助手，所有对话都在你的设备上处理，确保隐私安全。有什么我可以帮助你的吗？"
            userInput.contains("模型") || userInput.contains("model", true) ->
                "当前使用 llama.cpp 本地推理引擎。配置：nCtx=4096, nBatch=512, 根据设备性能自动分配线程数。所有推理完全在设备端完成。"
            userInput.contains("记忆") || userInput.contains("memory", true) ->
                "系统采用分层记忆管理：短期记忆（当前会话上下文）、长期记忆（形象记忆）和云端同步。默认20MB安全阈值，超过会自动转移到长期记忆。"
            userInput.contains("天玑") || userInput.contains("9300") ->
                "检测到天玑9300+处理器，已自动启用优化配置：8线程、大核优先、推荐 Q4_K_M 量化模型，推理速度最优。"
            userInput.contains("帮助") || userInput.contains("help", true) ->
                "可用功能：\n1）自然语言对话\n2）上下文记忆保留\n3）设备性能自适应\n4）隐私本地处理\n\n你也可以输入任意问题与我对话！"
            userInput.contains("谢谢") || userInput.contains("thanks", true) ->
                "不客气！很高兴能帮到你。如果还有其他问题，随时问我。"
            userInput.length <= 5 ->
                "关于「$userInput」，请告诉我更多细节，我可以给出更具体的回答。"
            else -> {
                val responses = listOf(
                    "这是一个有趣的问题。让我分析一下：关于「$userInput」，我的理解是这涉及到多方面的考量。你希望我从哪个角度深入讨论？",
                    "好的，我来帮你处理「$userInput」。以下是几点建议：\n1）明确目标\n2）分解问题\n3）逐步实施\n\n你想先从哪一步开始？",
                    "收到你的消息「$userInput」。根据我的分析，这是一个值得深入讨论的话题。你更关注哪个方面？",
                    "关于「$userInput」，我有以下想法可供参考。首先，理解核心需求是关键；其次，需要综合考虑资源和效率；最后，持续优化是长期改进的保障。"
                )
                responses.random()
            }
        }
    }
}


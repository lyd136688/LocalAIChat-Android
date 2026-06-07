package com.localai.chat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.localai.chat.ui.model.Agent
import com.localai.chat.ui.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow(
        listOf(
            ChatMessage(
                content = "你好！我是你的本地AI助手。\n\n我可以帮助你：\n• 回答问题\n• 编写代码\n• 翻译文本\n• 创意写作\n• 学习辅导\n\n直接输入问题开始对话吧！或点击「服务」选择专业服务。",
                isUser = false,
                timestamp = getTimeNow()
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentAgent = MutableStateFlow(
        Agent(name = "通用助手", emoji = "🤖", role = "default", description = "全能AI助手")
    )
    val currentAgent: StateFlow<Agent> = _currentAgent

    fun sendMessage(text: String) {
        val userMsg = ChatMessage(text, true, getTimeNow())
        _messages.value = listOf(userMsg) + _messages.value
        _isLoading.value = true

        viewModelScope.launch {
            delay(600)
            val response = generateResponse(text)
            _isLoading.value = false
            _messages.value = listOf(ChatMessage(response, false, getTimeNow())) + _messages.value
        }
    }

    private fun generateResponse(userInput: String): String {
        val input = userInput.trim()

        if (matchesAny(input, listOf("再见", "拜拜", "bye", "goodbye", "退出"))) {
            return "再见！如果还有其他问题，随时找我！👋"
        }
        if (matchesAny(input, listOf("你好", "您好", "嗨", "hello", "hi", "在吗"))) {
            return "你好！很高兴见到你。有什么我可以帮助你的吗？"
        }
        if (matchesAny(input, listOf("谢谢", "感谢", "thanks", "thank you"))) {
            return "不客气！能帮到你是我的荣幸。还有其他问题吗？"
        }
        if (matchesAny(input, listOf("代码", "编程", "程序", "bug", "报错", "code", "函数", "类", "python", "java", "kotlin"))) {
            return generateCodingResponse(input)
        }
        if (matchesAny(input, listOf("写", "文章", "作文", "邮件", "总结", "报告", "写个"))) {
            return generateWritingResponse(input)
        }
        if (matchesAny(input, listOf("翻译", "translate", "英文", "中文", "英语", "译成"))) {
            return "关于翻译请求「$input」：\n\n我可以帮助你进行多语言互译。请明确告诉我：\n1）源文本内容\n2）目标语言\n\n例如：「把这句话翻译成英文：今天天气真好」"
        }
        if (matchesAny(input, listOf("学习", "解释", "讲解", "教程", "什么是", "原理", "怎么", "如何"))) {
            return generateLearningResponse(input)
        }
        return when {
            input.length <= 5 -> "关于「$input」，请告诉我更多细节，比如你想了解什么方面？"
            input.length <= 15 -> generateConciseResponse(input)
            else -> generateDetailedResponse(input)
        }
    }

    private fun generateCodingResponse(input: String): String {
        return "收到你的编程相关问题「$input」。\n\n我来帮你分析：\n\n1）理解问题：你提到了编程/代码相关的内容\n2）解决思路：建议先明确目标和输入输出\n3）示例方案：\n\n```\n# 示例代码框架\ndef solution(input_data):\n    processed = preprocess(input_data)\n    result = core_logic(processed)\n    return result\n```\n\n如果你能提供更多具体信息（编程语言、目标、示例数据），我可以给出更精确的代码方案。"
    }

    private fun generateWritingResponse(input: String): String {
        return "好的，关于「$input」的写作需求：\n\n写作需要考虑以下几点：\n• 受众：写给谁看？\n• 目的：说明、说服、还是娱乐？\n• 长度：简短、中等、详细？\n• 风格：正式、友好、专业？\n\n请告诉我具体需求（例如「写一封商务邮件向客户介绍新产品」），我会为你量身定制。"
    }

    private fun generateLearningResponse(input: String): String {
        val topic = input.replace("怎么", "").replace("如何", "").replace("什么是", "").replace("解释", "").replace("讲解", "").trim()
        return """好的，让我为你讲解「$topic」：

📖 基础概念
$topic 指的是在特定领域中...

🔑 核心要点
1）理解基本定义
2）掌握关键特性
3）了解常见应用场景
4）学习最佳实践

💡 学习建议
• 从简单例子入手
• 逐步增加复杂度
• 多动手实践

如果你告诉我更具体的内容，我可以给出更详细的讲解。"""
    }

    private fun generateConciseResponse(input: String): String {
        val responses = listOf(
            "关于「$input」，我来帮你分析。这是一个很有意思的话题，让我整理一下思路：\n\n首先，需要明确问题的核心；\n其次，考虑可行的解决方案；\n最后，评估利弊并选择最优路径。\n\n你希望从哪个角度深入讨论？",
            "收到「$input」。\n\n这让我想到几个方向：\n1）从实际应用出发思考\n2）从原理层面理解\n3）从类比角度解释\n\n你更倾向哪种方式？或者告诉我具体想了解什么？",
            "好的，关于「$input」，我的理解是这是一个开放性的话题，有多个层面可以探讨。为了给出更有价值的回答，请告诉我：\n• 你的背景和目标\n• 你已经了解了什么\n• 你最关心哪方面"
        )
        return responses.random()
    }

    private fun generateDetailedResponse(input: String): String {
        val shortInput = input.take(50) + if (input.length > 50) "..." else ""
        return "你的问题「$shortInput」\n\n我已经认真分析了你的问题。以下是我的回答：\n\n📌 概述\n这是一个很好的问题。让我从几个角度来分析：\n\n🔍 详细分析\n1）核心问题：需要理解你描述的核心需求\n2）上下文考虑：结合实际场景进行分析\n3）可能的解决方案：多种路径\n\n💡 建议\n• 首先，明确你的目标\n• 其次，列出关键约束条件\n• 最后，评估可选项并决策\n\n如果你告诉我更多背景信息（你的角色、使用场景、期望结果），我可以给出更具体的建议。\n\n你想从哪里开始讨论？"
    }

    private fun matchesAny(input: String, keywords: List<String>): Boolean {
        val lower = input.lowercase(Locale.getDefault())
        return keywords.any { lower.contains(it.lowercase(Locale.getDefault())) }
    }

    private fun getTimeNow(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
}

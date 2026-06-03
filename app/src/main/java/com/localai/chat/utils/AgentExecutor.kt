package com.localai.chat.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AgentExecutor(private val context: Context) {
    
    private val memoryManager = MemoryManager(context)
    
    suspend fun execute(userInput: String): String {
        val tools = detectTools(userInput)
        
        if (tools.isEmpty()) {
            val response = generateResponse(userInput)
            memoryManager.addMemory("用户: $userInput", MemoryManager.TYPE_SHORT_TERM)
            memoryManager.addMemory("AI: $response", MemoryManager.TYPE_SHORT_TERM)
            return response
        }
        
        val toolResults = mutableListOf<String>()
        for (tool in tools) {
            val result = executeTool(tool, userInput)
            toolResults.add(result)
        }
        
        val contextText = toolResults.joinToString("\n")
        val response = generateResponseWithContext(userInput, contextText)
        
        memoryManager.addMemory("用户: $userInput", MemoryManager.TYPE_SHORT_TERM)
        memoryManager.addMemory("AI: $response", MemoryManager.TYPE_SHORT_TERM)
        
        return response
    }
    
    private fun detectTools(input: String): List<String> {
        val tools = mutableListOf<String>()
        
        if (input.contains("搜索", ignoreCase = true) ||
            input.contains("search", ignoreCase = true) ||
            input.contains("查询", ignoreCase = true)) {
            tools.add("web_search")
        }
        
        if (input.contains("计算", ignoreCase = true) ||
            input.contains("算", ignoreCase = true) ||
            input.matches(Regex(".*\\d+[+\\-*/].*"))) {
            tools.add("calculator")
        }
        
        if (input.contains("记忆", ignoreCase = true) ||
            input.contains("回忆", ignoreCase = true) ||
            input.contains("之前", ignoreCase = true)) {
            tools.add("memory_search")
        }
        
        return tools
    }
    
    private suspend fun executeTool(tool: String, input: String): String {
        return when (tool) {
            "web_search" -> {
                webSearch(input)
            }
            "calculator" -> {
                calculate(input)
            }
            "memory_search" -> {
                memorySearch(input)
            }
            else -> "未知工具: $tool"
        }
    }
    
    private suspend fun webSearch(query: String): String {
        return withContext(Dispatchers.IO) {
            "搜索结果: 关于「$query」的模拟搜索结果\n" +
            "在实际实现中，这里会调用搜索引擎API获取真实结果。"
        }
    }
    
    private fun calculate(input: String): String {
        return try {
            val expression = input.filter { it.isDigit() || it in "+-*/(). " }
            val result = evaluateExpression(expression)
            "计算结果: $result"
        } catch (e: Exception) {
            "计算错误: ${e.message}"
        }
    }
    
    private fun evaluateExpression(expression: String): Double {
        val tokens = expression.split(" ")
        var result = 0.0
        var op = '+'
        
        for (token in tokens) {
            if (token.isEmpty()) continue
            if (token in "+-*/") {
                op = token[0]
            } else {
                val num = token.toDouble()
                result = when (op) {
                    '+' -> result + num
                    '-' -> result - num
                    '*' -> result * num
                    '/' -> result / num
                    else -> num
                }
            }
        }
        return result
    }
    
    private suspend fun memorySearch(query: String): String {
        val memories = memoryManager.searchMemory(query)
        return if (memories.isNotEmpty()) {
            "找到 ${memories.size} 条相关记忆:\n" +
            memories.take(5).joinToString("\n") { "- ${it.content}" }
        } else {
            "未找到相关记忆"
        }
    }
    
    private suspend fun generateResponse(input: String): String {
        return withContext(Dispatchers.IO) {
            "这是AI对「$input」的模拟回复。\n" +
            "在实际实现中，这里会调用本地LLaMA模型进行推理生成。"
        }
    }
    
    private suspend fun generateResponseWithContext(input: String, context: String): String {
        return withContext(Dispatchers.IO) {
            "基于工具调用结果的AI回复:\n" +
            "问题: $input\n" +
            "参考信息: $context\n\n" +
            "综合分析后的模拟回复。实际实现中会使用本地模型推理。"
        }
    }
}


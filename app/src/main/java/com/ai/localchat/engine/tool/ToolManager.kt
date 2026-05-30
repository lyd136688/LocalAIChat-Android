package com.ai.localchat.engine.tool

import com.ai.localchat.engine.llama.LlamaEngine
import android.util.Log

interface Tool {
    val name: String
    val description: String
    fun execute(params: String): String
}

// 计算器工具
class CalculatorTool : Tool {
    override val name = "calculator"
    override val description = "执行数学计算，参数是数学表达式"

    override fun execute(params: String): String {
        return try {
            // 简单计算器实现
            val result = eval(params)
            "计算结果：$result"
        } catch (e: Exception) {
            "计算错误：${e.message}"
        }
    }

    private fun eval(expr: String): Double {
        return object : Any() {
            var pos = 0
            fun parse(): Double {
                val x = parseTerm()
                while (pos < expr.length) {
                    when (expr[pos]) {
                        '+' -> { pos++; x + parseTerm() }
                        '-' -> { pos++; x - parseTerm() }
                        else -> return x
                    }
                }
                return x
            }
            fun parseTerm(): Double {
                var x = parseFactor()
                while (pos < expr.length) {
                    when (expr[pos]) {
                        '*' -> { pos++; x *= parseFactor() }
                        '/' -> { pos++; x /= parseFactor() }
                        else -> return x
                    }
                }
                return x
            }
            fun parseFactor(): Double {
                if (expr[pos] == '(') {
                    pos++
                    val x = parse()
                    pos++
                    return x
                }
                val start = pos
                while (pos < expr.length && (expr[pos].isDigit() || expr[pos] == '.')) {
                    pos++
                }
                return expr.substring(start, pos).toDouble()
            }
        }.parse()
    }
}

// 工具管理器
object ToolManager {
    private const val TAG = "ToolManager"
    private val tools = listOf<Tool>(
        CalculatorTool()
        // 后续可以添加更多工具：文件读取、网络搜索等
    )

    private const val TOOL_CALL_PROMPT = """
你是一个智能助手，可以调用工具来完成任务。
可用工具：
{{tools}}

如果用户的问题需要使用工具，请按照以下格式输出：
[{"name":"工具名称","parameters":"参数"}]

如果不需要使用工具，直接回答用户的问题。

用户问题：{{userInput}}
"""

    // 检查是否需要调用工具
    fun checkAndCallTool(userInput: String): String? {
        if (!LlamaEngine.isModelLoaded()) return null

        return try {
            val toolsText = tools.joinToString("\n") {
                "- ${it.name}: ${it.description}"
            }

            val prompt = TOOL_CALL_PROMPT
                .replace("{{tools}}", toolsText)
                .replace("{{userInput}}", userInput)

            val result = LlamaEngine.chatInfer(prompt)
            
            // 检测工具调用标记
            if (result.contains("") && result.contains("")) {
                val start = result.indexOf("") + "".length
                val end = result.indexOf("")
                val toolCallJson = result.substring(start, end).trim()
                
                // 解析工具调用
                val pattern = """"name":"(.*?)","parameters":"(.*?)"""".toRegex()
                val matchResult = pattern.find(toolCallJson) ?: return null
                
                val toolName = matchResult.groupValues[1]
                val params = matchResult.groupValues[2]
                
                // 调用工具
                val tool = tools.find { it.name == toolName } ?: return null
                val toolResult = tool.execute(params)
                
                // 结合工具结果生成最终回答
                val finalPrompt = """
用户问题：$userInput
工具调用结果：$toolResult
请根据工具调用结果回答用户的问题。
"""
                return LlamaEngine.chatInfer(finalPrompt)
            } else {
                // 不需要调用工具，直接返回结果
                return result
            }
        } catch (e: Exception) {
            Log.e(TAG, "工具调用失败", e)
            return null
        }
    }
}


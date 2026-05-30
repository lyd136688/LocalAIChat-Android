package com.ai.localchat.engine.agent

import com.ai.localchat.engine.llama.LlamaEngine
import com.google.gson.Gson
import android.util.Log

object AgentGenerator {
    private const val TAG = "AgentGenerator"
    private val gson = Gson()

    private const val GENERATE_PROMPT = """
你是一个专业的AI Agent生成器。请严格按照以下要求生成Agent配置：

1. 只输出标准JSON格式，不要任何其他解释、说明或markdown
2. JSON必须包含以下四个字段：
   - agentName: 简洁的Agent名称（不超过10个字）
   - agentDesc: 一句话描述Agent的功能
   - promptTemplate: 详细的系统提示词，必须包含{{userInput}}占位符表示用户输入
   - isEnable: 固定为true

3. 系统提示词要专业、具体、针对性强，明确告诉AI应该做什么、怎么做
4. 不要在JSON中添加任何注释或额外字段

用户需求：{{userInput}}
"""

    // 根据用户需求生成Agent
    fun generateAgent(userRequest: String): AgentInfo? {
        if (!LlamaEngine.isModelLoaded()) {
            Log.e(TAG, "模型未加载，无法生成Agent")
            return null
        }

        return try {
            val prompt = GENERATE_PROMPT.replace("{{userInput}}", userRequest)
            val result = LlamaEngine.chatInfer(prompt)
            AgentManager.parseAgentFromJson(result, "")
        } catch (e: Exception) {
            Log.e(TAG, "生成Agent失败", e)
            null
        }
    }

    // 生成并保存Agent到本地
    fun generateAndSaveAgent(userRequest: String, saveDir: String): AgentInfo? {
        val agent = generateAgent(userRequest) ?: return null
        return if (AgentManager.parseAgentFromJson(gson.toJson(agent), saveDir) != null) {
            agent
        } else {
            null
        }
    }
}


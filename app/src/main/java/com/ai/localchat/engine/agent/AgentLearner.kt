package com.ai.localchat.engine.agent

import android.util.Log
import com.google.gson.Gson
import com.ai.localchat.model.AgentInfo
import com.ai.localchat.model.ChatRecord
import com.ai.localchat.engine.LlamaEngine
import com.ai.localchat.manager.AgentManager
import java.io.File

object AgentLearner {
    private const val TAG = "AgentLearner"
    private val gson = Gson()

    private const val OPTIMIZE_PROMPT = """
你是一个Agent优化专家。请根据以下历史对话，优化这个Agent的系统提示词，让它更符合用户的使用习惯和需求。

原Agent信息:
名称: {{agentName}}
描述: {{agentDesc}}
原提示词: {{oldPrompt}}

历史对话记录:
{{chatHistory}}

要求:
1. 保留原提示词的核心功能
2. 针对用户的提问方式和偏好进行优化
3. 让提示词更具体、更准确
4. 必须包含{{userInput}}占位符
5. 只输出优化后的提示词, 不要任何其他内容
"""

    // 根据历史对话优化Agent提示词
    fun optimizeAgent(agent: AgentInfo, chatHistory: List<ChatRecord>): AgentInfo? {
        if (!LlamaEngine.isModelLoaded() || chatHistory.isEmpty()) return null

        return try {
            val historyText = chatHistory.joinToString("\n\n") { record ->
                "用户: ${record.userText}\nAI: ${record.aiText}"
            }

            val prompt = OPTIMIZE_PROMPT
                .replace("{{agentName}}", agent.agentName)
                .replace("{{agentDesc}}", agent.agentDesc)
                .replace("{{oldPrompt}}", agent.promptTemplate)
                .replace("{{chatHistory}}", historyText)

            val optimizedPrompt = LlamaEngine.chatInfer(prompt).trim()

            // 验证优化后的提示词
            if (!optimizedPrompt.contains("{{userInput}}")) {
                Log.e(TAG, "优化后的提示词缺少{{userInput}}占位符")
                return null
            }

            val optimizedAgent = agent.copy(promptTemplate = optimizedPrompt)
            Log.d(TAG, "Agent ${agent.agentName} 优化完成")
            optimizedAgent
        } catch (e: Exception) {
            Log.e(TAG, "优化Agent失败", e)
            null
        }
    }

    // 自动学习并保存优化后的Agent
    fun learnAndSaveAgent(agent: AgentInfo, chatHistory: List<ChatRecord>, saveDir: String): Boolean {
        val optimizedAgent = optimizeAgent(agent, chatHistory) ?: return false

        return try {
            val json = gson.toJson(optimizedAgent)
            val file = File(saveDir, "${agent.agentName.replace(" ", "_")}.json")
            file.writeText(json)

            // 更新本地Agent列表
            AgentManager.reloadLocalAgents(saveDir)
            true
        } catch (e: Exception) {
            Log.e(TAG, "保存优化后的Agent失败", e)
            false
        }
    }
}


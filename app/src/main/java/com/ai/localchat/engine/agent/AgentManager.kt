package com.ai.localchat.engine.agent

import java.io.File
import com.google.gson.Gson

object AgentManager {
    private val agentList = mutableListOf<AgentInfo>()
    private var currentAgent: AgentInfo? = null
    private val gson = Gson()

    // 加载本地所有Agent配置文件
    fun loadLocalAgents(folderPath: String) {
        val dir = File(folderPath)
        if (!dir.exists()) dir.mkdirs()
        
        // 创建默认Agent配置（如果没有任何Agent）
        if (dir.listFiles()?.isEmpty() == true) {
            createDefaultAgent(folderPath)
        }
        
        dir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".json")) {
                try {
                    val json = file.readText()
                    val agent = gson.fromJson(json, AgentInfo::class.java)
                    agentList.add(agent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        // 默认选中第一个Agent
        if (agentList.isNotEmpty()) currentAgent = agentList.first()
    }

    // 创建默认通用助手Agent
    private fun createDefaultAgent(folderPath: String) {
        val defaultAgent = AgentInfo(
            agentName = "通用助手",
            agentDesc = "日常问答、图文解析",
            promptTemplate = "你是本地AI助手，请用中文简洁回答用户问题：{{userInput}}",
            isEnable = true
        )
        
        val json = gson.toJson(defaultAgent)
        val file = File(folderPath, "default_agent.json")
        file.writeText(json)
    }

    // 切换Agent
    fun switchAgent(name: String): Boolean {
        currentAgent = agentList.find { it.agentName == name }
        return currentAgent != null
    }

    // 拼接Agent提示词 + 用户输入
    fun buildPrompt(userInput: String): String {
        val agent = currentAgent ?: return userInput
        return agent.promptTemplate.replace("{{userInput}}", userInput)
    }

    fun getCurrentAgent(): AgentInfo? = currentAgent
    fun getAllAgents(): List<AgentInfo> = agentList
}


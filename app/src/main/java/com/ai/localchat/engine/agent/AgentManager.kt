// 从任意URL下载并解析Agent
suspend fun downloadAgentFromUrl(url: String, saveDir: String): AgentInfo? {
    return try {
        val request = Request.Builder()
            .url(url)
            .build()
        
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null
        
        val json = response.body?.string() ?: return null
        return parseAgentFromJson(json, saveDir)
    } catch (e: Exception) {
        Log.e(TAG, "从URL下载Agent失败", e)
        null
    }
}

// 从JSON字符串解析并保存Agent
fun parseAgentFromJson(json: String, saveDir: String): AgentInfo? {
    return try {
        // 智能提取JSON（处理大模型输出的带额外内容的JSON）
        val jsonStart = json.indexOf("{")
        val jsonEnd = json.lastIndexOf("}") + 1
        if (jsonStart == -1 || jsonEnd == 0) return null
        
        val cleanJson = json.substring(jsonStart, jsonEnd)
        val agent = gson.fromJson(cleanJson, AgentInfo::class.java)
        
        // 验证Agent有效性
        if (agent.agentName.isBlank() || agent.promptTemplate.isBlank()) return null
        
        // 保存到本地
        val file = File(saveDir, "${agent.agentName.replace(" ", "_")}.json")
        file.writeText(cleanJson)
        
        // 添加到本地列表
        agentList.add(agent)
        Log.d(TAG, "Agent ${agent.agentName} 解析并保存成功")
        agent
    } catch (e: Exception) {
        Log.e(TAG, "解析Agent失败", e)
        null
    }
}

// 从内置浏览器获取当前页面内容并尝试解析Agent
suspend fun parseAgentFromBrowserPage(pageUrl: String, saveDir: String): AgentInfo? {
    return downloadAgentFromUrl(pageUrl, saveDir)
}
// 重新加载本地所有Agent
fun reloadLocalAgents(folderPath: String) {
    agentList.clear()
    loadLocalAgents(folderPath)
}

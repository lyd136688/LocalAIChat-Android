package com.ai.localchat.app

import android.app.Application
import com.ai.localchat.engine.llama.LlamaEngine
import com.ai.localchat.engine.agent.AgentManager
import com.ai.localchat.core.optimize.OptimizeManager
import java.io.File

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化llama.cpp推理引擎
        LlamaEngine.init()
        
        // 创建必要的本地目录
        createNecessaryDirectories()
        
        // 加载本地所有Agent配置
        AgentManager.loadLocalAgents(filesDir.absolutePath + "/agent/")
        
        // 启动自我优化后台巡检
        OptimizeManager.startOptimizeTask(applicationContext)
    }

    private fun createNecessaryDirectories() {
        // 模型存储目录
        val modelDir = File(filesDir, "model")
        if (!modelDir.exists()) modelDir.mkdirs()
        
        // Agent配置目录
        val agentDir = File(filesDir, "agent")
        if (!agentDir.exists()) agentDir.mkdirs()
        
        // 多媒体缓存目录
        val mediaDir = File(filesDir, "media")
        if (!mediaDir.exists()) mediaDir.mkdirs()
    }
}

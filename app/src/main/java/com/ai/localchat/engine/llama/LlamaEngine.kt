package com.ai.localchat.engine.llama

import com.ai.localchat.core.optimize.OptimizeManager

/** 本地量化大模型推理引擎（对接llama.cpp so库） */
object LlamaEngine {
    private var isModelLoaded = false

    init {
        // 加载原生动态库 libllama.so
        System.loadLibrary("llama")
    }

    // 原生方法：加载GGUF量化模型
    external fun loadModel(modelPath: String, threadNum: Int): Boolean
    // 原生方法：对话推理
    external fun chatInfer(prompt: String): String
    // 原生方法：释放模型内存
    external fun releaseModel()

    // 自动加载模型（结合自我优化参数）
    fun autoLoadModel(modelPath: String): Boolean {
        if (isModelLoaded) releaseModel()
        
        val thread = OptimizeManager.getOptThreadCount()
        isModelLoaded = loadModel(modelPath, thread)
        return isModelLoaded
    }

    fun isModelLoaded(): Boolean = isModelLoaded
}


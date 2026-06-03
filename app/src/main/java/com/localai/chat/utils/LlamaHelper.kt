package com.localai.chat.utils

import android.content.Context
import java.io.File

class LlamaHelper(private val context: Context) {
    
    private var isLoaded = false
    private var currentModel: String? = null
    
    fun loadModel(modelPath: String): Boolean {
        val file = File(modelPath)
        if (!file.exists()) return false
        
        try {
            currentModel = modelPath
            isLoaded = true
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    fun generate(prompt: String, maxTokens: Int = 512): String {
        if (!isLoaded) {
            return "模型未加载，请先下载并加载模型"
        }
        
        return try {
            "这是模拟的AI回复。当前使用的模型: ${currentModel?.substringAfterLast("/")}\n" +
            "收到的问题: $prompt\n\n" +
            "在实际实现中，这里会调用LLaMA.cpp的JNI接口进行推理。"
        } catch (e: Exception) {
            "推理出错: ${e.message}"
        }
    }
    
    fun unloadModel() {
        isLoaded = false
        currentModel = null
    }
    
    fun isModelLoaded(): Boolean = isLoaded
    
    fun getCurrentModel(): String? = currentModel
    
    fun getAvailableModels(): List<String> {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) return emptyList()
        
        return modelsDir.listFiles()
            ?.filter { it.extension in listOf("gguf", "bin", "ggml") }
            ?.map { it.absolutePath }
            ?: emptyList()
    }
}

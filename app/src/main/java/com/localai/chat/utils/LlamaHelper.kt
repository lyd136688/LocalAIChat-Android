package com.localai.chat.utils

import android.content.Context
import java.io.File

class LlamaHelper(private val context: Context) {
    
    private val bridge = LlamaBridge()
    private var currentModelPath: String? = null
    private var currentModelName: String? = null
    
    /**
     * 加载GGUF模型
     */
    fun loadModel(modelPath: String): Boolean {
        if (!File(modelPath).exists()) {
            return false
        }
        
        val success = bridge.loadModel(modelPath)
        if (success) {
            currentModelPath = modelPath
            currentModelName = File(modelPath).nameWithoutExtension
        }
        return success
    }
    
    /**
     * 生成回复
     */
    fun generate(prompt: String, maxTokens: Int = 512): String {
        if (!bridge.isModelLoaded()) {
            return "模型未加载，请先下载并加载模型"
        }
        
        return try {
            bridge.generate(
                prompt = prompt,
                maxTokens = maxTokens,
                temperature = 0.7f
            )
        } catch (e: Exception) {
            "推理出错: ${e.message}"
        }
    }
    
    /**
     * 卸载模型释放内存
     */
    fun unloadModel() {
        bridge.unloadModel()
        currentModelPath = null
        currentModelName = null
    }
    
    /**
     * 检查是否已加载模型
     */
    fun isModelLoaded(): Boolean = bridge.isModelLoaded()
    
    /**
     * 获取当前模型名称
     */
    fun getCurrentModel(): String? = currentModelName
    
    /**
     * 获取模型占用内存
     */
    fun getModelMemorySize(): String {
        val bytes = bridge.getModelSize()
        return when {
            bytes >= 1024L * 1024 * 1024 * 1024 -> String.format("%.2f TB", bytes / (1024.0 * 1024 * 1024 * 1024))
            bytes >= 1024L * 1024 * 1024 -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024L * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> "$bytes B"
        }
    }
    
    /**
     * 获取设备上所有可用模型
     */
    fun getAvailableModels(): List<String> {
        val modelsDir = File(context.filesDir, "models")
        if (!modelsDir.exists()) return emptyList()
        
        return modelsDir.listFiles()
            ?.filter { it.extension.lowercase() in listOf("gguf", "ggml", "bin") }
            ?.map { it.absolutePath }
            ?: emptyList()
    }
    
    /**
     * 获取模型存放目录
     */
    fun getModelsDirectory(): File {
        return File(context.filesDir, "models").apply { mkdirs() }
    }
}


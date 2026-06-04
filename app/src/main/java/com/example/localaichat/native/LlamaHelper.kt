package com.example.localaichat.native

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Llama 推理帮助类 - 管理模型加载和推理
 */
class LlamaHelper(private val context: Context) {
    private val tag = "LlamaHelper"
    
    // 模型状态回调
    interface ModelCallback {
        fun onModelLoaded(success: Boolean, info: String)
        fun onModelUnloaded()
    }
    
    var callback: ModelCallback? = null
    
    init {
        // 初始化 JNI
        val success = LlamaBridge.nativeInit()
        Log.i(tag, "JNI init result: $success")
    }
    
    /**
     * 加载模型文件
     */
    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        Log.i(tag, "Loading model: $modelPath")
        
        val file = File(modelPath)
        if (!file.exists()) {
            Log.e(tag, "Model file not found: $modelPath")
            return@withContext false
        }
        
        val success = LlamaBridge.nativeLoadModel(modelPath)
        val info = if (success) LlamaBridge.nativeGetModelInfo() else "Failed to load model"
        
        withContext(Dispatchers.Main) {
            callback?.onModelLoaded(success, info)
        }
        
        success
    }
    
    /**
     * 生成回复
     */
    suspend fun generate(prompt: String, maxTokens: Int = 256): String = withContext(Dispatchers.IO) {
        if (!LlamaBridge.nativeIsModelLoaded()) {
            return@withContext "Error: Model not loaded"
        }
        
        LlamaBridge.nativeGenerate(prompt, maxTokens)
    }
    
    /**
     * 检查模型是否已加载
     */
    fun isModelLoaded(): Boolean {
        return LlamaBridge.nativeIsModelLoaded()
    }
    
    /**
     * 卸载模型
     */
    fun unloadModel() {
        LlamaBridge.nativeUnloadModel()
        callback?.onModelUnloaded()
    }
    
    /**
     * 释放资源
     */
    fun release() {
        LlamaBridge.nativeFree()
    }
    
    /**
     * 获取模型存放目录
     */
    fun getModelDir(): File {
        return File(context.getExternalFilesDir(null), "models").apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * 获取模型文件路径
     */
    fun getModelPath(modelName: String): String {
        return File(getModelDir(), "$modelName.gguf").absolutePath
    }
}


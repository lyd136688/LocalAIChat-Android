package com.localai.chat.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 混合推理调度器
 * 根据网络状态、用户设置、模型可用性自动选择推理后端
 */
class InferenceScheduler(private val context: Context) {
    
    enum class Backend {
        LOCAL,      // 本地llama.cpp推理
        CLOUD_HF,   // HuggingFace Inference API
        CLOUD_MS,   // ModelScope API
        FALLBACK    // 降级到模拟回复
    }
    
    data class InferenceResult(
        val text: String,
        val backend: Backend,
        val latencyMs: Long,
        val tokensPerSecond: Float?
    )
    
    private val llamaHelper = LlamaHelper(context)
    private val cloudApi = CloudInferenceApi()
    
    /**
     * 执行推理，自动选择最优后端
     */
    suspend fun infer(
        prompt: String,
        preferredBackend: Backend? = null,
        maxTokens: Int = 512
    ): InferenceResult {
        val startTime = System.currentTimeMillis()
        
        // 1. 如果指定了首选后端，优先尝试
        preferredBackend?.let {
            val result = tryBackend(it, prompt, maxTokens)
            if (result != null) {
                return result.copy(latencyMs = System.currentTimeMillis() - startTime)
            }
        }
        
        // 2. 检查本地模型是否可用
        if (llamaHelper.isModelLoaded()) {
            val result = tryBackend(Backend.LOCAL, prompt, maxTokens)
            if (result != null) {
                return result.copy(latencyMs = System.currentTimeMillis() - startTime)
            }
        }
        
        // 3. 检查网络，尝试云端
        if (isNetworkAvailable()) {
            // 优先HuggingFace
            val hfResult = tryBackend(Backend.CLOUD_HF, prompt, maxTokens)
            if (hfResult != null) {
                return hfResult.copy(latencyMs = System.currentTimeMillis() - startTime)
            }
            
            // 备选ModelScope
            val msResult = tryBackend(Backend.CLOUD_MS, prompt, maxTokens)
            if (msResult != null) {
                return msResult.copy(latencyMs = System.currentTimeMillis() - startTime)
            }
        }
        
        // 4. 降级到模拟回复
        return InferenceResult(
            text = generateFallbackResponse(prompt),
            backend = Backend.FALLBACK,
            latencyMs = System.currentTimeMillis() - startTime,
            tokensPerSecond = null
        )
    }
    
    /**
     * 尝试指定后端推理
     */
    private suspend fun tryBackend(
        backend: Backend,
        prompt: String,
        maxTokens: Int
    ): InferenceResult? = withContext(Dispatchers.IO) {
        try {
            when (backend) {
                Backend.LOCAL -> {
                    if (!llamaHelper.isModelLoaded()) return@withContext null
                    val response = llamaHelper.generate(prompt, maxTokens)
                    InferenceResult(
                        text = response,
                        backend = Backend.LOCAL,
                        latencyMs = 0,
                        tokensPerSecond = 15.5f // 模拟本地推理速度
                    )
                }
                Backend.CLOUD_HF -> {
                    val response = cloudApi.inferenceHuggingFace(prompt, maxTokens)
                    InferenceResult(
                        text = response,
                        backend = Backend.CLOUD_HF,
                        latencyMs = 0,
                        tokensPerSecond = null // 云端不统计
                    )
                }
                Backend.CLOUD_MS -> {
                    val response = cloudApi.inferenceModelScope(prompt, maxTokens)
                    InferenceResult(
                        text = response,
                        backend = Backend.CLOUD_MS,
                        latencyMs = 0,
                        tokensPerSecond = null
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 加载本地模型
     */
    fun loadLocalModel(modelPath: String): Boolean {
        return llamaHelper.loadModel(modelPath)
    }
    
    /**
     * 卸载本地模型释放内存
     */
    fun unloadLocalModel() {
        llamaHelper.unloadModel()
    }
    
    /**
     * 检查本地模型是否已加载
     */
    fun isLocalModelLoaded(): Boolean = llamaHelper.isModelLoaded()
    
    /**
     * 获取当前模型信息
     */
    fun getCurrentModelInfo(): String {
        return if (llamaHelper.isModelLoaded()) {
            "本地模型: ${llamaHelper.getCurrentModel()}\n内存占用: ${llamaHelper.getModelMemorySize()}"
        } else {
            "未加载本地模型"
        }
    }
    
    /**
     * 检查网络是否可用
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * 降级回复
     */
    private fun generateFallbackResponse(prompt: String): String {
        return "【离线模式】\n\n" +
               "当前无可用推理后端：\n" +
               "• 本地模型未加载\n" +
               "• 网络连接不可用\n\n" +
               "您可以：\n" +
               "1. 下载并加载本地模型\n" +
               "2. 连接网络使用云端API\n\n" +
               "您的问题: $prompt"
    }
    
    /**
     * 云端推理API封装
     */
    private inner class CloudInferenceApi {
        
        suspend fun inferenceHuggingFace(prompt: String, maxTokens: Int): String {
            // 实际实现：调用HF Inference API
            // 需要API Token
            return "【HuggingFace云端回复】\n\n$prompt\n\n" +
                   "(实际部署需要配置API Token)"
        }
        
        suspend fun inferenceModelScope(prompt: String, maxTokens: Int): String {
            // 实际实现：调用ModelScope API
            return "【ModelScope云端回复】\n\n$prompt\n\n" +
                   "(实际部署需要配置API Token)"
        }
    }
}

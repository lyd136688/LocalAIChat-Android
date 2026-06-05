package com.example.localaichat.native

import android.content.Context
import android.util.Log
import com.example.localaichat.data.model.ChatMessage
import com.example.localaichat.storage.ImageMemoryStorage
import com.example.localaichat.storage.SessionMemoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 混合推理调度器
 * 
 * 策略：
 * - 高端设备（天玑9300+）→ 优先本地推理
 * - 中端设备 → 根据模型大小决定
 * - 低端设备 → 优先云端 API
 * - 本地失败 → 自动 fallback 到云端
 */
class InferenceScheduler(
    private val context: Context,
    private val llamaHelper: LlamaHelper,
    private val sessionMemory: SessionMemoryManager,
    private val imageMemoryStorage: ImageMemoryStorage
) {
    companion object {
        const val TAG = "InferenceScheduler"

        enum class InferenceMode {
            LOCAL,      // 本地推理
            CLOUD,      // 云端 API
            HYBRID      // 混合模式
        }
    }

    private val deviceProfile = DeviceProfileDetector.detect(context)
    private val streamingGenerator = StreamingGenerator(llamaHelper, sessionMemory, imageMemoryStorage)

    // 调度状态
    private val _schedulerState = MutableStateFlow(SchedulerState())
    val schedulerState: StateFlow<SchedulerState> = _schedulerState

    // 当前推理模式
    var currentMode: InferenceMode = detectBestMode()

    /**
     * 自动检测最佳推理模式
     */
    private fun detectBestMode(): InferenceMode {
        return when {
            deviceProfile.name == "Dimensity 9300+" -> {
                Log.i(TAG, "Dimensity 9300+ detected, using LOCAL mode")
                InferenceMode.LOCAL
            }
            deviceProfile.name == "High-End" -> {
                Log.i(TAG, "High-end device, using HYBRID mode")
                InferenceMode.HYBRID
            }
            deviceProfile.name == "Mid-Range" -> {
                Log.i(TAG, "Mid-range device, using HYBRID mode")
                InferenceMode.HYBRID
            }
            else -> {
                Log.i(TAG, "Low-end device, using CLOUD mode")
                InferenceMode.CLOUD
            }
        }
    }

    /**
     * 发送消息并获取回复
     */
    suspend fun chat(
        userMessage: String,
        onChunk: (String) -> Unit,
        onComplete: (String, Boolean) -> Unit
    ) {
        _schedulerState.value = _schedulerState.value.copy(
            isProcessing = true,
            currentMode = currentMode.name
        )

        try {
            when (currentMode) {
                InferenceMode.LOCAL -> {
                    chatLocal(userMessage, onChunk, onComplete)
                }
                InferenceMode.CLOUD -> {
                    chatCloud(userMessage, onComplete)
                }
                InferenceMode.HYBRID -> {
                    chatHybrid(userMessage, onChunk, onComplete)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Inference error", e)
            _schedulerState.value = _schedulerState.value.copy(
                isProcessing = false,
                error = e.message
            )
            onComplete("推理出错: ${e.message}", true)
        }
    }

    /**
     * 本地推理
     */
    private suspend fun chatLocal(
        userMessage: String,
        onChunk: (String) -> Unit,
        onComplete: (String, Boolean) -> Unit
    ) {
        if (!llamaHelper.isModelLoaded()) {
            Log.w(TAG, "Model not loaded, falling back to cloud")
            chatCloud(userMessage, onComplete)
            return
        }

        streamingGenerator.generateStreaming(
            prompt = userMessage,
            onChunk = onChunk,
            onComplete = { response, truncated ->
                _schedulerState.value = _schedulerState.value.copy(
                    isProcessing = false,
                    lastResponseTokens = response.length,
                    inferenceMode = "LOCAL"
                )
                onComplete(response, truncated)
            }
        )
    }

    /**
     * 云端 API 推理
     */
    private suspend fun chatCloud(
        userMessage: String,
        onComplete: (String, Boolean) -> Unit
    ) {
        // TODO: 接入 HuggingFace / ModelScope API
        // 暂时返回提示
        _schedulerState.value = _schedulerState.value.copy(
            isProcessing = false,
            inferenceMode = "CLOUD"
        )
        onComplete("云端 API 尚未接入，请先下载本地模型。", false)
    }

    /**
     * 混合推理：先尝试本地，失败则云端
     */
    private suspend fun chatHybrid(
        userMessage: String,
        onChunk: (String) -> Unit,
        onComplete: (String, Boolean) -> Unit
    ) {
        if (llamaHelper.isModelLoaded()) {
            try {
                chatLocal(userMessage, onChunk, onComplete)
                return
            } catch (e: Exception) {
                Log.w(TAG, "Local inference failed, falling back to cloud: ${e.message}")
            }
        }
        // Fallback 到云端
        chatCloud(userMessage, onComplete)
    }

    /**
     * 切换推理模式
     */
    fun switchMode(mode: InferenceMode) {
        currentMode = mode
        Log.i(TAG, "Switched to ${mode.name} mode")
        _schedulerState.value = _schedulerState.value.copy(currentMode = mode.name)
    }

    /**
     * 获取设备信息
     */
    fun getDeviceInfo(): String {
        return """
            设备配置: ${deviceProfile.name}
            推理模式: ${currentMode.name}
            上下文长度: ${deviceProfile.nCtx}
            批处理大小: ${deviceProfile.nBatch}
            线程数: ${deviceProfile.nThreads}
            GPU 层数: ${deviceProfile.gpuLayers}
            NPU: ${deviceProfile.useNpu}
            GPU: ${deviceProfile.useGpu}
        """.trimIndent()
    }
}

data class SchedulerState(
    val isProcessing: Boolean = false,
    val currentMode: String = "HYBRID",
    val inferenceMode: String = "",
    val lastResponseTokens: Int = 0,
    val error: String? = null
)

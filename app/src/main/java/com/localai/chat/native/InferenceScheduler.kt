package com.localai.chat.native

import android.content.Context
import android.util.Log
import com.localai.chat.storage.ImageMemoryStorage
import com.localai.chat.storage.SessionMemoryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InferenceScheduler(
    private val context: Context,
    private val llamaHelper: LlamaHelper,
    private val sessionMemory: SessionMemoryManager,
    private val imageMemoryStorage: ImageMemoryStorage
) {
    companion object {
        const val TAG = "InferenceScheduler"

        enum class InferenceMode {
            LOCAL, CLOUD, HYBRID
        }
    }

    private val deviceProfile = DeviceProfileDetector.detect(context)
    private val streamingGenerator = StreamingGenerator(llamaHelper, sessionMemory, imageMemoryStorage)

    private val _schedulerState = MutableStateFlow(SchedulerState())
    val schedulerState: StateFlow<SchedulerState> = _schedulerState

    var currentMode: InferenceMode = detectBestMode()

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
                InferenceMode.LOCAL -> chatLocal(userMessage, onChunk, onComplete)
                InferenceMode.CLOUD -> chatCloud(userMessage, onComplete)
                InferenceMode.HYBRID -> chatHybrid(userMessage, onChunk, onComplete)
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

    private suspend fun chatCloud(
        userMessage: String,
        onComplete: (String, Boolean) -> Unit
    ) {
        _schedulerState.value = _schedulerState.value.copy(
            isProcessing = false,
            inferenceMode = "CLOUD"
        )
        onComplete("云端 API 尚未接入，请先下载本地模型。", false)
    }

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
        chatCloud(userMessage, onComplete)
    }

    fun switchMode(mode: InferenceMode) {
        currentMode = mode
        Log.i(TAG, "Switched to ${mode.name} mode")
        _schedulerState.value = _schedulerState.value.copy(currentMode = mode.name)
    }

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

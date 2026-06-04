package com.example.localaichat.native

import android.util.Log
import com.example.localaichat.storage.ImageMemoryStorage
import com.example.localaichat.storage.StorageConfig
import com.example.localaichat.storage.SessionMemoryManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StreamingGenerator(
    private val llamaHelper: LlamaHelper,
    private val sessionMemory: SessionMemoryManager,
    private val imageMemoryStorage: ImageMemoryStorage
) {
    companion object {
        const val TAG = "StreamingGenerator"
    }

    private var isGenerating = false
    private var currentJob: Job? = null

    // 生成状态
    private val _generationState = MutableStateFlow(GenerationState())
    val generationState: StateFlow<GenerationState> = _generationState

    /**
     * 流式生成 - 防崩溃核心方法
     */
    suspend fun generateStreaming(
        prompt: String,
        onChunk: (String) -> Unit,
        onComplete: (String, Boolean) -> Unit
    ) {
        if (isGenerating) {
            Log.w(TAG, "Generation already in progress")
            return
        }

        isGenerating = true
        val output = StringBuilder()
        var totalBytes = 0
        var isTruncated = false

        _generationState.value = GenerationState(
            isGenerating = true,
            generatedBytes = 0,
            progress = 0f
        )

        try {
            // 添加用户消息到会话
            sessionMemory.addMessage(
                com.example.localaichat.data.model.ChatMessage(
                    id = "user_${System.currentTimeMillis()}",
                    sessionId = "current",
                    role = "user",
                    content = prompt,
                    timestamp = System.currentTimeMillis()
                )
            )

            // 构建完整上下文
            val fullPrompt = sessionMemory.getFormattedContext()

            // 调用本地推理，逐 token 生成
            llamaHelper.generateStreaming(fullPrompt) { chunk ->
                output.append(chunk)
                totalBytes += chunk.toByteArray(Charsets.UTF_8).size

                // 更新进度
                val progress = totalBytes.toFloat() / StorageConfig.STREAM_MAX_SAFE_SIZE
                _generationState.value = _generationState.value.copy(
                    generatedBytes = totalBytes,
                    progress = progress.coerceIn(0f, 1f)
                )

                // 回调输出片段
                onChunk(chunk)

                // 安全检查：超过20MB安全阈值
                if (totalBytes >= StorageConfig.STREAM_MAX_SAFE_SIZE) {
                    Log.w(TAG, "Reached safe limit (${totalBytes / (1024*1024)}MB), stopping generation")
                    isTruncated = true
                    stopGeneration()
                }
            }

            val response = output.toString()

            // 保存助手回复
            val assistantMessage = com.example.localaichat.data.model.ChatMessage(
                id = "assistant_${System.currentTimeMillis()}",
                sessionId = "current",
                role = "assistant",
                content = response,
                timestamp = System.currentTimeMillis()
            )

            sessionMemory.addMessage(assistantMessage)

            // 通过形象记忆系统保存（自动判断是否转存）
            imageMemoryStorage.saveMessage(assistantMessage)

            _generationState.value = GenerationState(
                isGenerating = false,
                generatedBytes = totalBytes,
                progress = 1f,
                isTruncated = isTruncated
            )

            onComplete(response, isTruncated)

        } catch (e: Exception) {
            Log.e(TAG, "Generation error", e)
            _generationState.value = _generationState.value.copy(
                isGenerating = false,
                error = e.message
            )
            onComplete(output.toString(), true)
        } finally {
            isGenerating = false
        }
    }

    /**
     * 停止生成
     */
    fun stopGeneration() {
        currentJob?.cancel()
        currentJob = null
        isGenerating = false
    }

    /**
     * 是否正在生成
     */
    fun isGenerating(): Boolean = isGenerating
}

data class GenerationState(
    val isGenerating: Boolean = false,
    val generatedBytes: Int = 0,
    val progress: Float = 0f,
    val isTruncated: Boolean = false,
    val error: String? = null
)

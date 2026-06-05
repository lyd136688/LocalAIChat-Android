package com.localai.chat.native

import android.util.Log
import com.localai.chat.storage.ImageMemoryStorage
import com.localai.chat.storage.SessionMemoryManager
import com.localai.chat.storage.StorageConfig
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
    private var isTruncated = false

    private val _generationState = MutableStateFlow(GenerationState())
    val generationState: StateFlow<GenerationState> = _generationState

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
        isTruncated = false
        val output = StringBuilder()
        var totalBytes = 0

        _generationState.value = GenerationState(
            isGenerating = true,
            generatedBytes = 0,
            progress = 0f
        )

        try {
            sessionMemory.addMessage(
                com.localai.chat.data.model.ChatMessage(
                    id = "user_${System.currentTimeMillis()}",
                    sessionId = "current",
                    role = "user",
                    content = prompt,
                    timestamp = System.currentTimeMillis()
                )
            )

            val fullPrompt = sessionMemory.getFormattedContext()

            llamaHelper.generateStreaming(fullPrompt) { chunk ->
                output.append(chunk)
                totalBytes += chunk.toByteArray(Charsets.UTF_8).size

                val progress = totalBytes.toFloat() / StorageConfig.STREAM_MAX_SAFE_SIZE
                _generationState.value = _generationState.value.copy(
                    generatedBytes = totalBytes,
                    progress = progress.coerceIn(0f, 1f)
                )

                onChunk(chunk)

                if (totalBytes >= StorageConfig.STREAM_MAX_SAFE_SIZE) {
                    Log.w(TAG, "Reached safe limit (${totalBytes / (1024*1024)}MB), stopping generation")
                    isTruncated = true
                    llamaHelper.unloadModel()
                }
            }

            val response = output.toString()

            val assistantMessage = com.localai.chat.data.model.ChatMessage(
                id = "assistant_${System.currentTimeMillis()}",
                sessionId = "current",
                role = "assistant",
                content = response,
                timestamp = System.currentTimeMillis()
            )

            sessionMemory.addMessage(assistantMessage)
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

    fun stopGeneration() {
        isTruncated = true
        llamaHelper.unloadModel()
    }

    fun isGenerating(): Boolean = isGenerating
}

data class GenerationState(
    val isGenerating: Boolean = false,
    val generatedBytes: Int = 0,
    val progress: Float = 0f,
    val isTruncated: Boolean = false,
    val error: String? = null
)

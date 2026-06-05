package com.localai.chat.native

import android.content.Context
import android.util.Log
import com.localai.chat.storage.StorageConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LlamaHelper(private val context: Context) {

    private val tag = "LlamaHelper"
    var deviceProfile: DeviceProfile = DeviceProfileDetector.detect(context)

    interface ModelCallback {
        fun onModelLoaded(success: Boolean, info: String)
        fun onModelUnloaded()
    }

    var callback: ModelCallback? = null

    init {
        val success = LlamaBridge.nativeInit()
        Log.i(tag, "JNI init result: $success, device profile: ${deviceProfile.name}")
    }

    suspend fun loadModel(modelPath: String): Boolean = withContext(Dispatchers.IO) {
        Log.i(tag, "Loading model: $modelPath")
        Log.i(tag, "Device config: n_ctx=${deviceProfile.nCtx}, n_batch=${deviceProfile.nBatch}, threads=${deviceProfile.nThreads}")

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

    suspend fun generate(prompt: String, maxTokens: Int = 256): String = withContext(Dispatchers.IO) {
        if (!LlamaBridge.nativeIsModelLoaded()) {
            return@withContext "Error: Model not loaded"
        }
        LlamaBridge.nativeGenerate(prompt, maxTokens)
    }

    fun generateStreaming(
        prompt: String,
        maxTokens: Int = 2048,
        onToken: (String) -> Unit
    ) {
        LlamaBridge.nativeGenerateStreaming(prompt, maxTokens, object : LlamaBridge.TokenCallback {
            override fun onToken(token: String) {
                onToken(token)
            }
        })
    }

    fun isModelLoaded(): Boolean {
        return LlamaBridge.nativeIsModelLoaded()
    }

    fun unloadModel() {
        LlamaBridge.nativeUnloadModel()
        callback?.onModelUnloaded()
    }

    fun release() {
        LlamaBridge.nativeFree()
    }

    fun getModelDir(): File {
        return File(context.getExternalFilesDir(null), "models").apply {
            if (!exists()) mkdirs()
        }
    }

    fun getModelPath(modelName: String): String {
        return File(getModelDir(), "$modelName.gguf").absolutePath
    }
}


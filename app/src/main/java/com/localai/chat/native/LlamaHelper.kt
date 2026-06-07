package com.localai.chat.native

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LlamaHelper {
    private var handle: Long = 0
    
    suspend fun init(): Boolean = withContext(Dispatchers.IO) { LlamaBridge.nativeInit() }
    
    suspend fun loadModel(path: String): Boolean = withContext(Dispatchers.IO) {
        handle = LlamaBridge.nativeLoadModel(path)
        handle != 0L
    }
    
    fun isLoaded(): Boolean = LlamaBridge.nativeIsModelLoaded(handle)
    
    suspend fun generate(prompt: String, maxTokens: Int = 512): String = withContext(Dispatchers.IO) {
        LlamaBridge.nativeGenerate(handle, prompt, maxTokens)
    }
    
    fun unload() { LlamaBridge.nativeUnloadModel(handle) }
    fun free() { LlamaBridge.nativeFree(handle) }
}

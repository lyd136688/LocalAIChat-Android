package com.localai.chat.native

object LlamaBridge {
    init {
        System.loadLibrary("localaichat_jni")
    }
    
    external fun nativeInit(): Boolean
    external fun nativeLoadModel(modelPath: String): Long
    external fun nativeIsModelLoaded(handle: Long): Boolean
    external fun nativeGenerate(handle: Long, prompt: String, maxTokens: Int): String
    external fun nativeUnloadModel(handle: Long)
    external fun nativeFree(handle: Long)
    external fun nativeGetModelInfo(handle: Long): String
}

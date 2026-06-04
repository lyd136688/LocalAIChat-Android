package com.example.localaichat.native

import android.util.Log

/**
 * JNI 桥接类 - 封装 llama.cpp 本地推理
 */
object LlamaBridge {
    private const val TAG = "LlamaBridge"
    
    // 加载 JNI 库
    init {
        try {
            System.loadLibrary("localaichat_jni")
            Log.i(TAG, "JNI library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load JNI library", e)
            throw RuntimeException("Cannot load native library", e)
        }
    }
    
    // JNI 方法声明
    external fun nativeInit(): Boolean
    external fun nativeLoadModel(modelPath: String): Boolean
    external fun nativeIsModelLoaded(): Boolean
    external fun nativeGenerate(prompt: String, maxTokens: Int): String
    external fun nativeUnloadModel()
    external fun nativeFree()
    external fun nativeGetModelInfo(): String
}

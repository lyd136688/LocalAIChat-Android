package com.localai.chat.native

import android.util.Log

object LlamaBridge {
    private const val TAG = "LlamaBridge"

    init {
        try {
            System.loadLibrary("localaichat_jni")
            Log.i(TAG, "JNI library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load JNI library", e)
            throw RuntimeException("Cannot load native library", e)
        }
    }

    external fun nativeInit(): Boolean
    external fun nativeLoadModel(modelPath: String): Boolean
    external fun nativeIsModelLoaded(): Boolean
    external fun nativeGenerate(prompt: String, maxTokens: Int): String
    external fun nativeUnloadModel()
    external fun nativeFree()
    external fun nativeGetModelInfo(): String

    external fun nativeGenerateStreaming(prompt: String, maxTokens: Int, callback: TokenCallback)

    interface TokenCallback {
        fun onToken(token: String)
    }
}

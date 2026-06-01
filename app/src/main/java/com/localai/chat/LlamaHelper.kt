package com.localai.chat

class LlamaHelper(private val modelPath: String) {
    init {
        System.loadLibrary("llama")
    }

    private external fun nativeLoadModel(path: String): Boolean
    private external fun nativeGenerate(prompt: String): String
    private external fun nativeUnloadModel()

    private var loaded = false

    fun generate(prompt: String): String {
        if (!loaded) {
            loaded = nativeLoadModel(modelPath)
            if (!loaded) return "模型加载失败"
        }
        return nativeGenerate(prompt)
    }

    protected fun finalize() {
        if (loaded) nativeUnloadModel()
    }
}

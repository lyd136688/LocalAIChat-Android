package com.localai.chat.utils

class LlamaBridge {
    
    companion object {
        init {
            System.loadLibrary("llama")
        }
    }
    
    /**
     * 加载模型
     * @param modelPath 模型文件路径 (.gguf)
     * @return 是否加载成功
     */
    external fun loadModel(modelPath: String): Boolean
    
    /**
     * 生成文本
     * @param prompt 提示词
     * @param maxTokens 最大生成token数
     * @param temperature 温度参数
     * @return 生成的文本
     */
    external fun generate(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f
    ): String
    
    /**
     * 卸载模型
     */
    external fun unloadModel()
    
    /**
     * 检查模型是否已加载
     */
    external fun isModelLoaded(): Boolean
    
    /**
     * 获取模型占用内存大小
     */
    external fun getModelSize(): Long
}

package com.localai.chat

class LlamaHelper(private val modelPath: String) {
    // 临时版本：不加载任何 native 库，返回模拟回复
    fun generate(prompt: String): String {
        return "[模拟回复] 您说的是：$prompt\n（当前为演示模式，集成真实模型后生效）"
    }

    fun generateWithImage(imagePath: String, prompt: String): String {
        return "[模拟回复] 收到图片：$imagePath\n问题：$prompt\n（图片识别需要多模态模型）"
    }
}

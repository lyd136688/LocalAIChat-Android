package com.localai.chat.native

class StreamingGenerator(private val llamaHelper: LlamaHelper) {
    suspend fun generateStreaming(prompt: String, onToken: (String) -> Unit) {
        val result = llamaHelper.generate(prompt)
        onToken(result)
    }
}

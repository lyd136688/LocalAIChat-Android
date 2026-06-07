package com.localai.chat.native

class InferenceScheduler(private val llamaHelper: LlamaHelper) {
    suspend fun generate(prompt: String): String = llamaHelper.generate(prompt)
}

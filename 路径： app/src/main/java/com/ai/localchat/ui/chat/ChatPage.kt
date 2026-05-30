// 普通聊天逻辑
if (LlamaEngine.isModelLoaded()) {
    val prompt = AgentManager.buildPrompt(inputText)
    outputText = "正在思考..."
    
    coroutineScope.launch(Dispatchers.IO) {
        // 先检查是否需要调用工具
        val result = ToolManager.checkAndCallTool(prompt) ?: LlamaEngine.chatInfer(prompt)
        
        withContext(Dispatchers.Main) {
            outputText = result
        }
    }
} else {
    outputText = "模型未加载，请先在模型页面加载模型"
}


@Composable
fun ChatPage() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("模型未加载，请先在模型页面加载模型") }
    var isGeneratingAgent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = outputText,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("输入问题或生成Agent指令") },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = {
                if (inputText.isBlank()) return@Button
                
                // 检测是否是生成Agent的指令
                if (inputText.startsWith("生成Agent") || inputText.startsWith("创建Agent")) {
                    isGeneratingAgent = true
                    outputText = "正在生成Agent..."
                    
                    coroutineScope.launch(Dispatchers.IO) {
                        val saveDir = context.filesDir.absolutePath + "/agent/"
                        val agent = AgentGenerator.generateAndSaveAgent(inputText, saveDir)
                        
                        withContext(Dispatchers.Main) {
                            isGeneratingAgent = false
                            outputText = if (agent != null) {
                                AgentManager.switchAgent(agent.agentName)
                                "✅ Agent ${agent.agentName} 生成成功并已启用\n${agent.agentDesc}"
                            } else {
                                "❌ 生成Agent失败，请重试"
                            }
                        }
                    }
                } else {
                    // 普通聊天逻辑
                    if (LlamaEngine.isModelLoaded()) {
                        val prompt = AgentManager.buildPrompt(inputText)
                        outputText = "正在思考..."
                        
                        coroutineScope.launch(Dispatchers.IO) {
                            val result = LlamaEngine.chatInfer(prompt)
                            withContext(Dispatchers.Main) {
                                outputText = result
                            }
                        }
                    } else {
                        outputText = "模型未加载，请先在模型页面加载模型"
                    }
                }
            },
            modifier = Modifier.padding(top = 8.dp),
            enabled = !isGeneratingAgent
        ) {
            Text(if (isGeneratingAgent) "生成中..." else "发送")
        }
    }
}


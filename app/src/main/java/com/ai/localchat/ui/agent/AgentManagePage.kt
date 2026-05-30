package com.ai.localchat.ui.agent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ai.localchat.core.browser.BrowserActivity // 必须导入这个包
import com.ai.localchat.engine.agent.AgentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AgentManagePage() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var agentUrl by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }
    // 刷新本地Agent列表
    var refreshTrigger by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 1.4 测试按钮：打开内置浏览器
        Button(
            onClick = {
                // 这里就是你问的1.4的代码
                BrowserActivity.start(
                    context,
                    "https://huggingface.co/models",
                    "模型&Agent下载站"
                )
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("打开浏览器找Agent/模型")
        }

        OutlinedTextField(
            value = agentUrl,
            onValueChange = { agentUrl = it },
            label = { Text("输入Agent JSON链接") },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = {
                if (agentUrl.isBlank()) return@Button
                statusText = "正在下载Agent..."
                
                coroutineScope.launch(Dispatchers.IO) {
                    val saveDir = context.filesDir.absolutePath + "/agent/"
                    val agent = AgentManager.downloadAgentFromUrl(agentUrl, saveDir)
                    
                    withContext(Dispatchers.Main) {
                        statusText = if (agent != null) {
                            refreshTrigger++ // 刷新列表
                            "✅ Agent ${agent.agentName} 下载成功"
                        } else {
                            "❌ 下载失败，请检查链接"
                        }
                    }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("从URL下载Agent")
        }

        Text(text = statusText)

        // 本地Agent列表
        Text(
            text = "本地Agent列表",
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        // 每次refreshTrigger变化时重新获取列表
        val localAgents = remember(refreshTrigger) {
            AgentManager.getAllAgents()
        }

        localAgents.forEach { agent ->
            Button(
                onClick = {
                    AgentManager.switchAgent(agent.agentName)
                    statusText = "✅ 已切换到 ${agent.agentName}"
                },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(text = agent.agentName)
            }
        }
    }
}


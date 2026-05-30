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
import com.ai.localchat.core.browser.BrowserActivity
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                BrowserActivity.start(context, "https://github.com/topics/ai-agent-json", "Agent资源站")
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("打开浏览器找Agent")
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
                            "Agent ${agent.agentName} 下载成功"
                        } else {
                            "下载失败，请检查链接"
                        }
                    }
                }
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("从URL下载Agent")
        }

        Text(text = statusText)

        // 本地Agent列表（原有代码）
        Text(
            text = "本地Agent列表",
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        AgentManager.getAllAgents().forEach { agent ->
            Button(
                onClick = {
                    AgentManager.switchAgent(agent.agentName)
                    statusText = "已切换到 ${agent.agentName}"
                },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(text = agent.agentName)
            }
        }
    }
}


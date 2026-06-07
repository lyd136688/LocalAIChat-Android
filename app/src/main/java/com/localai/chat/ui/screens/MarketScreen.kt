package com.localai.chat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Agent(
    val name: String,
    val emoji: String,
    val role: String,
    val description: String
)

private val agents = listOf(
    Agent("通用助手", "🤖", "default", "全能AI助手"),
    Agent("编程专家", "💻", "programmer", "专注编程与技术"),
    Agent("写作大师", "✍️", "writer", "文章创作润色"),
    Agent("翻译官", "🌐", "translator", "多语言互译专家"),
    Agent("学习导师", "📚", "tutor", "学习辅导与答疑"),
    Agent("数据分析师", "📊", "analyst", "数据分析与可视化")
)

private val models = listOf(
    "Llama-2-7B-Chat (已下载)",
    "Qwen-7B (推荐)",
    "Mistral-7B-Instruct",
    "Phi-2 (快速推理)"
)

@Composable
fun MarketScreen() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Agent市场", "模型市场")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color(0xFF1E1E1E),
            contentColor = Color.White
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index },
                    selectedContentColor = Color(0xFF6200EE),
                    unselectedContentColor = Color(0xFF888888)
                )
            }
        }
        if (tabIndex == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) { items(agents) { agent -> AgentCard(agent) } }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) { items(models) { model -> ModelCard(model) } }
        }
    }
}

@Composable
fun AgentCard(agent: Agent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(agent.emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(agent.name, color = Color.White, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(agent.description, color = Color(0xFF888888), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6200EE))
                ) { Text("使用此Agent", fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun ModelCard(model: String) {
    val isDownloaded = model.contains("已下载")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("📦", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(model.replace(" (已下载)", ""), color = Color.White, fontSize = 14.sp)
                Text("本地推理模型", color = Color(0xFF888888), fontSize = 11.sp)
            }
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDownloaded) Color(0xFF4CAF50) else Color(0xFF6200EE)
                )
            ) { Text(if (isDownloaded) "加载" else "下载", fontSize = 12.sp) }
        }
    }
}

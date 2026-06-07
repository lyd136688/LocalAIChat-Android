package com.localai.chat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Service(val title: String, val icon: String, val description: String)

private val services = listOf(
    Service("智能问答", "💬", "回答各类问题"),
    Service("创意写作", "✍️", "文章创作"),
    Service("编程助手", "💻", "代码编写调试"),
    Service("翻译工具", "🌐", "多语言互译"),
    Service("数据分析", "📊", "数据报告"),
    Service("学习辅导", "📚", "课程讲解"),
    Service("会议总结", "📝", "整理会议要点"),
    Service("邮件写作", "📧", "商务邮件")
)

@Composable
fun ServicesScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = Color(0xFF1E1E1E)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("智能服务", color = Color.White, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("选择一个服务快速开始", color = Color(0xFF888888), fontSize = 12.sp)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(services) { service ->
                ServiceCard(service)
            }
        }
    }
}

@Composable
fun ServiceCard(service: Service) {
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp).clickable { },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = service.icon, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = service.title, color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = service.description, color = Color(0xFF888888), fontSize = 11.sp, textAlign = TextAlign.Center)
        }
    }
}


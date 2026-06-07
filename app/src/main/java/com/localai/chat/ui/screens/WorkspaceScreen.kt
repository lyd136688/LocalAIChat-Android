package com.localai.chat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkspaceScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = Color(0xFF1E1E1E)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("工作区", color = Color.White, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("管理模型、设置与本地资源", color = Color(0xFF888888), fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 模型设置
            item {
                SettingsGroup("模型设置") {
                    SettingsItem("当前模型", "Llama-2-7B-Chat")
                    SettingsItem("量化等级", "Q4_K_M")
                    SettingsItem("上下文长度", "4096 tokens")
                    SettingsItem("线程数", "自动 (8)")
                }
            }

            // 对话设置
            item {
                SettingsGroup("对话设置") {
                    SettingsItem("温度", "0.7")
                    SettingsItem("最大输出", "2048 tokens")
                    SettingsItem("Top-P", "0.9")
                }
            }

            // 系统信息
            item {
                SettingsGroup("系统信息") {
                    SettingsItem("应用版本", "1.0.0")
                    SettingsItem("本地模型数", "1")
                    SettingsItem("存储占用", "3.2 GB")
                    SettingsItem("设备性能", "天玑 9300+")
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            color = Color(0xFF888888),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(text = value, color = Color(0xFF888888), fontSize = 12.sp)
    }
}

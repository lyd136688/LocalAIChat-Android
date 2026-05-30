package com.ai.localchat.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.ai.localchat.R
import com.ai.localchat.ui.chat.ChatPage
import com.ai.localchat.ui.model.ModelManagePage
import com.ai.localchat.ui.agent.AgentManagePage
import com.ai.localchat.ui.history.HistoryPage
import com.ai.localchat.ui.setting.SettingPage

@Composable
fun AppNavHost() {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val tabList = listOf("聊天", "模型", "Agent", "记忆", "设置")

    Scaffold(
        bottomBar = {
            BottomAppBar {
                tabList.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            when (index) {
                                0 -> Icon(painterResource(id = R.drawable.ic_chat), contentDescription = null)
                                1 -> Icon(painterResource(id = R.drawable.ic_model), contentDescription = null)
                                2 -> Icon(painterResource(id = R.drawable.ic_agent), contentDescription = null)
                                3 -> Icon(painterResource(id = R.drawable.ic_history), contentDescription = null)
                                4 -> Icon(painterResource(id = R.drawable.ic_setting), contentDescription = null)
                            }
                        },
                        label = { Text(text = title) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 切换对应页面
            when (selectedIndex) {
                0 -> ChatPage()
                1 -> ModelManagePage()
                2 -> AgentManagePage()
                3 -> HistoryPage()
                4 -> SettingPage()
            }
        }
    }
}

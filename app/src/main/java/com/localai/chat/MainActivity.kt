package com.localai.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.localai.chat.ui.screens.ChatScreen
import com.localai.chat.ui.screens.ServicesScreen
import com.localai.chat.ui.screens.MarketScreen
import com.localai.chat.ui.screens.WorkspaceScreen
import com.localai.chat.ui.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val chatViewModel = ChatViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainApp(chatViewModel)
                }
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val label: String, val icon: String) {
    object Chat : BottomNavItem("chat", "对话", "💬")
    object Services : BottomNavItem("services", "服务", "🎯")
    object Market : BottomNavItem("market", "市场", "🏪")
    object Workspace : BottomNavItem("workspace", "工作区", "⚙️")
}

@Composable
fun MainApp(chatViewModel: ChatViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Chat,
        BottomNavItem.Services,
        BottomNavItem.Market,
        BottomNavItem.Workspace
    )

    Scaffold(
        containerColor = Color(0xFF121212),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1E1E1E)) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = { Text(item.icon, style = MaterialTheme.typography.titleMedium) },
                        label = { Text(item.label, color = Color.White) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = Color(0xFF6200EE),
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF2A2A2A)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Chat.route) { ChatScreen(chatViewModel) }
            composable(BottomNavItem.Services.route) { ServicesScreen() }
            composable(BottomNavItem.Market.route) { MarketScreen() }
            composable(BottomNavItem.Workspace.route) { WorkspaceScreen() }
        }
    }
}

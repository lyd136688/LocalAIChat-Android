package com.localai.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
            MaterialTheme {
                Surface(color = Color(0xFF121212)) {
                    MainApp(chatViewModel)
                }
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Chat : BottomNavItem("chat", "对话", Icons.Default.Chat)
    object Services : BottomNavItem("services", "服务", Icons.Default.Search)
    object Market : BottomNavItem("market", "市场", Icons.Default.Home)
    object Workspace : BottomNavItem("workspace", "工作区", Icons.Default.Workspaces)
}

@OptIn(ExperimentalMaterialApi::class)
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
        bottomBar = {
            BottomNavigation(
                backgroundColor = Color(0xFF1E1E1E),
                contentColor = Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    BottomNavigationItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        selectedContentColor = Color(0xFF6200EE),
                        unselectedContentColor = Color.Gray
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

package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.JervesBgDark
import com.example.ui.theme.JervesCyanContainer
import com.example.ui.theme.JervesCyanPrimary
import com.example.ui.theme.JervesSurfaceBorder
import com.example.ui.theme.JervesSurfaceDark
import com.example.ui.theme.JervesTextSecondary
import com.example.ui.viewmodel.JervesViewModel

@Composable
fun JervesApp(
    viewModel: JervesViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

    // Custom back handler: return to dashboard if on any secondary screen
    BackHandler(enabled = currentScreen != Screen.DASHBOARD) {
        currentScreen = Screen.DASHBOARD
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JervesBgDark),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = JervesBgDark,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .border(
                        1.dp,
                        JervesSurfaceBorder,
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ),
                containerColor = JervesSurfaceDark,
                contentColor = JervesCyanPrimary
            ) {
                Screen.entries.forEach { screen ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JervesCyanPrimary,
                            selectedTextColor = JervesCyanPrimary,
                            indicatorColor = JervesCyanContainer,
                            unselectedIconColor = JervesTextSecondary,
                            unselectedTextColor = JervesTextSecondary
                        ),
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { targetScreen ->
                when (targetScreen) {
                    Screen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it }
                    )
                    Screen.CHAT -> ChatScreen(viewModel = viewModel)
                    Screen.TASKS -> TasksScreen(viewModel = viewModel)
                    Screen.MEMORY -> MemoryScreen(viewModel = viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

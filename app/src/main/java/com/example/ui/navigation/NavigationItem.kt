package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("HUD", Icons.Default.RadioButtonChecked, "nav_dashboard"),
    CHAT("Neural", Icons.Default.ChatBubble, "nav_chat"),
    TASKS("Directives", Icons.Default.Assignment, "nav_tasks"),
    MEMORY("Matrix", Icons.Default.Memory, "nav_memory"),
    SETTINGS("System", Icons.Default.Settings, "nav_settings")
}

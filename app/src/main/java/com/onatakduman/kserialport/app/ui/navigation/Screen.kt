package com.onatakduman.kserialport.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Terminal : Screen("terminal", "Terminal", Icons.Filled.Terminal, Icons.Outlined.Terminal)
    data object Recordings : Screen("recordings", "Recordings", Icons.Filled.FolderOpen, Icons.Outlined.FolderOpen)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    data object About : Screen("about", "About", Icons.Filled.Info, Icons.Outlined.Info)

    companion object {
        val topLevelScreens = listOf(Home, Terminal, Recordings, Settings)
    }
}

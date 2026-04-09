package com.example.apptensionnel.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Accueil", Icons.Default.Home)
    object History : Screen("history", "Historique", Icons.Default.History)
    object Trends : Screen("trends", "Tendances", Icons.Default.ShowChart)
    object Settings : Screen("settings", "Paramètres", Icons.Default.Settings)
}

val navItems = listOf(
    Screen.Home,
    Screen.Trends,
    Screen.History,
    Screen.Settings
)

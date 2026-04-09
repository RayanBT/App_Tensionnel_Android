package com.example.apptensionnel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.apptensionnel.data.PreferenceManager
import com.example.apptensionnel.ui.navigation.Screen
import com.example.apptensionnel.ui.navigation.navItems
import com.example.apptensionnel.ui.screens.AddMeasurementScreen
import com.example.apptensionnel.ui.screens.HistoryScreen
import com.example.apptensionnel.ui.screens.HomeScreen
import com.example.apptensionnel.ui.screens.SettingsScreen
import com.example.apptensionnel.ui.screens.TrendsScreen
import com.example.apptensionnel.ui.theme.AppTensionnelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTensionnelTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // On ne montre la BottomBar que sur les écrans principaux
    val showBottomBar = navItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    navItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                if (currentDestination?.route != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(if (showBottomBar) innerPadding else androidx.compose.foundation.layout.PaddingValues(0.dp))
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    preferenceManager = preferenceManager,
                    onNavigateToAdd = { navController.navigate("add_measurement") }
                ) 
            }
            composable(Screen.Trends.route) { TrendsScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Settings.route) { SettingsScreen(preferenceManager) }
            
            // Écran d'ajout (Plein écran)
            composable("add_measurement") {
                AddMeasurementScreen(
                    preferenceManager = preferenceManager,
                    onBack = { navController.popBackStack() },
                    onSave = { navController.popBackStack() }
                )
            }
        }
    }
}

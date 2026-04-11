package com.example.apptensionnel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.apptensionnel.data.models.Measurement
import com.example.apptensionnel.ui.navigation.Screen
import com.example.apptensionnel.ui.navigation.navItems
import com.example.apptensionnel.ui.screens.*
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

    var editingMeasurement by remember { mutableStateOf<Measurement?>(null) }
    
    // Déterminer la destination de départ : si aucun profil n'existe, aller vers la création. 
    // Si des profils existent mais aucun n'est sélectionné, aller vers la sélection.
    val profiles = preferenceManager.getProfiles()
    val startRoute = when {
        profiles.isEmpty() -> "add_profile"
        preferenceManager.currentProfileId == null -> "profile_selection"
        else -> Screen.Home.route
    }

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
            startDestination = startRoute,
            modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp))
        ) {
            composable("profile_selection") {
                ProfileSelectionScreen(
                    preferenceManager = preferenceManager,
                    onProfileSelected = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo("profile_selection") { inclusive = true }
                        }
                    },
                    onNavigateToAddProfile = {
                        navController.navigate("add_profile")
                    }
                )
            }

            composable("add_profile") {
                AddProfileScreen(
                    preferenceManager = preferenceManager,
                    onProfileAdded = {
                        navController.navigate("profile_selection") {
                            popUpTo("add_profile") { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        if (preferenceManager.getProfiles().isNotEmpty()) {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(Screen.Home.route) { 
                HomeScreen(
                    preferenceManager = preferenceManager,
                    onNavigateToAdd = { 
                        editingMeasurement = null
                        navController.navigate("add_measurement") 
                    }
                ) 
            }
            composable(Screen.Trends.route) { TrendsScreen(preferenceManager) }
            composable(Screen.History.route) { 
                HistoryScreen(
                    preferenceManager = preferenceManager,
                    onEdit = { measurement ->
                        editingMeasurement = measurement
                        navController.navigate("add_measurement")
                    }
                ) 
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    preferenceManager = preferenceManager,
                    onNavigateToProfileSelection = {
                        navController.navigate("profile_selection") {
                            // On ne vide pas forcément toute la pile, mais on veut pouvoir revenir ou changer
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    }
                ) 
            }
            
            composable("add_measurement") {
                AddMeasurementScreen(
                    preferenceManager = preferenceManager,
                    measurementToEdit = editingMeasurement,
                    onBack = { 
                        editingMeasurement = null
                        navController.popBackStack() 
                    },
                    onSave = { 
                        editingMeasurement = null
                        navController.popBackStack() 
                    }
                )
            }
        }
    }
}

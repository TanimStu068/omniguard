package com.tanim.omniguard.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.tanim.omniguard.presentation.screens.*
import com.tanim.omniguard.presentation.viewmodel.DashboardViewModel
import com.tanim.omniguard.presentation.screens.*
import com.tanim.omniguard.presentation.screens.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val screens = listOf(
        Screen.Dashboard,
        Screen.Sentinel,
        Screen.Performance,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom bar for main top-level screens
    val showBottomBar = screens.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "main_root",
            modifier = Modifier.padding(if (showBottomBar) paddingValues else androidx.compose.foundation.layout.PaddingValues(0.dp))
        ) {
            navigation(startDestination = Screen.Scan.route, route = "main_root") {
                // Scan Screen with shared ViewModel
                composable(Screen.Scan.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main_root")
                    }
                    val dashboardViewModel: DashboardViewModel = hiltViewModel(parentEntry)
                    ScanScreen(navController, dashboardViewModel)
                }

                // Dashboard Screen with shared ViewModel
                composable(Screen.Dashboard.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main_root")
                    }
                    val dashboardViewModel: DashboardViewModel = hiltViewModel(parentEntry)
                    DashboardScreen(navController, dashboardViewModel)
                }

                composable(Screen.Sentinel.route) {
                    SentinelScreen(navController)
                }
                composable(Screen.Performance.route) {
                    PerformanceScreen(navController)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(navController)
                }

                // App Detail Screen
                composable(
                    route = Screen.AppDetail.route,
                    arguments = Screen.AppDetail.arguments
                ) { backStackEntry ->
                    val packageName = backStackEntry.arguments?.getString("packageName") ?: return@composable
                    AppDetailScreen(
                        navController = navController,
                        packageName = packageName
                    )
                }

                // About OmniGuard Screen
                composable(Screen.About.route) {
                    AboutOmniGuardScreen(navController = navController)
                }

                // Privacy Policy Screen
                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen(navController = navController)
                }

                // Open Source Licenses Screen
                composable(Screen.Licenses.route) {
                    OpenSourceLicensesScreen(navController = navController)
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Scan : Screen("scan", "Scan", Icons.Default.Search)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Sentinel : Screen("sentinel", "Sentinel", Icons.Default.Security)
    object Performance : Screen("performance", "Performance", Icons.Default.Speed)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object AppDetail : Screen("app_detail/{packageName}", "App Details", Icons.Default.Settings) {
        val arguments = listOf(navArgument("packageName") { type = NavType.StringType })
    }

    object About : Screen("about", "About", Icons.Default.Info)
    object PrivacyPolicy : Screen("privacy_policy", "Privacy", Icons.Default.PrivacyTip)
    object Licenses : Screen("licenses", "Licenses", Icons.Default.Code)
}
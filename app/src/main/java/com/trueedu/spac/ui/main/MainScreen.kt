package com.trueedu.spac.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trueedu.spac.ui.home.HomeScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.tabs.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = currentDestination?.hasRoute(destination::class) == true,
                    onClick = {
                        navController.navigate(destination) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = AppDestinations.Home,
            modifier = Modifier.fillMaxSize()
        ) {
            composable<AppDestinations.Home> {
                HomeScreen()
            }
            composable<AppDestinations.Favorites> {
                TodoScreen("Favorites")
            }
            composable<AppDestinations.Profile> {
                TodoScreen("Profile")
            }

            // composable<AppDestinations.StockDetail> { backStackEntry ->
            //     val stockDetail: AppDestinations.StockDetail = backStackEntry.toRoute()
            //     StockDetailScreen(stockId = stockDetail.stockId)
            // }
        }
    }
}

@Composable
fun TodoScreen(screenName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("TODO: $screenName Screen")
    }
}

package com.trueedu.spac.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.trueedu.spac.ui.following.FollowingScreen
import com.trueedu.spac.ui.home.HomeScreen
import com.trueedu.spac.ui.profile.ProfileScreen
import com.trueedu.spac.ui.search.SearchScreen

@Composable
fun MainScreen(
    navController: NavHostController,
    loginWithGoogle: () -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val openSearch = { page: Int ->
        navController.navigate(AppDestinations.Search(page))
    }

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
                            popUpTo(navController.graph.id) {
                                inclusive = true
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
            composable<AppDestinations.Home>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/home" }
                )
            ) {
                HomeScreen(
                )
            }
            composable<AppDestinations.Following>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/following" }
                )
            ) {
                FollowingScreen(
                    openSearch = openSearch,
                    openEdit = {
                        // TODO
                    },
                )
            }
            composable<AppDestinations.Profile>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/profile" }
                )
            ) {
                ProfileScreen(
                    loginWithGoogle = loginWithGoogle
                )
            }

            composable<AppDestinations.Search>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/search/{page}" }
                )
            ) { backStackEntry ->
                val search: AppDestinations.Search = backStackEntry.toRoute()
                SearchScreen(
                    currentPage = search.page,
                    onBack = { navController.popBackStack() },
                )
            }

            // composable<AppDestinations.StockDetail> { backStackEntry ->
            //     val stockDetail: AppDestinations.StockDetail = backStackEntry.toRoute()
            //     StockDetailScreen(stockId = stockDetail.stockId)
            // }
        }
    }
}

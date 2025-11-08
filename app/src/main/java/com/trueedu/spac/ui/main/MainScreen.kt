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
import com.trueedu.spac.LoginCallback
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.data.user.LocalUserCycle
import com.trueedu.spac.ui.admin.AdminScreen
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar
import com.trueedu.spac.ui.dart.DartScreen
import com.trueedu.spac.ui.edit.EditAssetScreen
import com.trueedu.spac.ui.following.FollowingScreen
import com.trueedu.spac.ui.home.HomeScreen
import com.trueedu.spac.ui.profile.MoreScreen
import com.trueedu.spac.ui.refund.RefundScheduleScreen
import com.trueedu.spac.ui.search.SearchScreen
import com.trueedu.spac.ui.stock.StockDetailScreen

@Composable
fun MainScreen(
    navController: NavHostController,
    simpleSnackbar: SimpleSnackbar,
    openUrl: (String) -> Unit,
    gotoPlayStore: () -> Unit,
    loginWithGoogle: (LoginCallback) -> Unit,
) {
    val trueAnalytics = LocalTrueAnalytics.current
    val userCycle = LocalUserCycle.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val openSearch = { page: Int ->
        if (userCycle.loggedIn()) {
            navController.navigate(AppDestinations.Search(page))
        } else {
            loginWithGoogle {
                navController.navigate(AppDestinations.Search(page))
            }
        }
    }

    val openStockDetail = { stockId: String ->
        trueAnalytics.log("stock_detail", mapOf("stockId" to stockId))
        navController.navigate(AppDestinations.StockDetail(stockId))
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
                    openStockDetail = openStockDetail,
                )
            }
            composable<AppDestinations.Following>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/following" }
                )
            ) {
                FollowingScreen(
                    loginWithGoogle = loginWithGoogle,
                    openSearch = openSearch,
                    openStockDetail = openStockDetail,
                )
            }
            composable<AppDestinations.More>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/more" }
                )
            ) {
                MoreScreen(
                    simpleSnackbar = simpleSnackbar,
                    gotoPlayStore = gotoPlayStore,
                    openDartScreen = {
                        navController.navigate(AppDestinations.Dart)
                    },
                    openRefundSchedule = {
                        navController.navigate(AppDestinations.RefundSchedule)
                    },
                    loginWithGoogle = loginWithGoogle,
                    openAdmin = {
                        navController.navigate(AppDestinations.Admin)
                    }
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

            composable<AppDestinations.StockDetail> { backStackEntry ->
                val stockDetail: AppDestinations.StockDetail = backStackEntry.toRoute()
                StockDetailScreen(
                    stockId = stockDetail.stockId,
                    editAssets = {
                        if (userCycle.loggedIn()) {
                            navController.navigate(AppDestinations.EditAsset(stockDetail.stockId))
                        } else {
                            loginWithGoogle {
                                navController.navigate(AppDestinations.EditAsset(stockDetail.stockId))
                            }
                        }
                    },
                    openUrl = openUrl,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<AppDestinations.Dart>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/dart" }
                )
            ) {
                DartScreen(
                    openUrl = openUrl,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<AppDestinations.EditAsset> { backStackEntry ->
                val editAsset = backStackEntry.toRoute<AppDestinations.EditAsset>()
                EditAssetScreen(
                    stockId = editAsset.stockId,
                    simpleSnackbar = simpleSnackbar,
                    onBack = { navController.popBackStack() },
                )
            }

            composable<AppDestinations.Admin> {
                AdminScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<AppDestinations.RefundSchedule>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/refund" }
                )
            ) {
                RefundScheduleScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

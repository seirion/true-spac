package com.trueedu.spac.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.trueedu.spac.ui.ads.AdmobManager
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar
import com.trueedu.spac.ui.dart.DartScreen
import com.trueedu.spac.ui.edit.EditAssetScreen
import com.trueedu.spac.ui.feedback.FeedbackScreen
import com.trueedu.spac.ui.following.FollowingScreen
import com.trueedu.spac.ui.home.HomeScreen
import com.trueedu.spac.ui.merge.MergeScheduleScreen
import com.trueedu.spac.ui.notification.NotificationScreen
import com.trueedu.spac.ui.profile.MoreScreen
import com.trueedu.spac.ui.refund.ManageRefundScheduleScreen
import com.trueedu.spac.ui.refund.RefundScheduleScreen
import com.trueedu.spac.ui.search.SearchScreen
import com.trueedu.spac.ui.stock.StockDetailScreen

private const val MAX_CLEAR_NON_TAB_BACKSTACK_GUARD = 100

/**
 * 탭(Home/Following/More) 전환 시 탭 외 목적지(예: Search/StockDetail/Admin 등)는 상태를 유지하지 않기 위해,
 * 현재 back stack을 "탭 루트 목적지"가 나올 때까지 정리한다.
 *
 * - 탭 루트 3개는 아래 navigate 옵션(popUpTo + saveState/restoreState)로만 유지된다.
 * - 탭 외 화면은 popBackStack()으로 제거되어 restore 대상이 되지 않는다.
 */
private fun NavHostController.clearNonTabBackStack(tabs: List<AppDestinations>) {
    fun isOnTabDestination(): Boolean {
        val dest = currentDestination
        return tabs.any { tab -> dest?.hasRoute(tab::class) == true }
    }

    // 비정상적인 back stack 상태에서 무한 루프를 방지하기 위한 안전장치
    var guard = 0
    while (!isOnTabDestination() && guard++ < MAX_CLEAR_NON_TAB_BACKSTACK_GUARD) {
        if (!popBackStack()) break
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
    admobManager: AdmobManager,
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

    val openStockDetail = { stockId: String, followingGroupPage: Int? ->
        trueAnalytics.log("stock_detail", mapOf("stockId" to stockId))
        navController.navigate(AppDestinations.StockDetail(stockId, followingGroupPage))
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
                        // 탭 외 화면(Search/StockDetail 등)은 유지하지 않고, 탭 루트까지만 남긴다.
                        navController.clearNonTabBackStack(AppDestinations.tabs)

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
            composable<AppDestinations.Home>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/home" }
                )
            ) {
                HomeScreen(
                    admobManager = admobManager,
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
                    openMergeSchedule = {
                        navController.navigate(AppDestinations.MergeSchedule)
                    },
                    openFeedback = {
                        navController.navigate(AppDestinations.Feedback)
                    },
                    loginWithGoogle = loginWithGoogle,
                    openNotification = {
                        navController.navigate(AppDestinations.Notification)
                    },
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
                    followingGroupPage = stockDetail.followingGroupPage,
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
                    onBack = { navController.popBackStack() },
                    openManageRefundSchedule = {
                        navController.navigate(AppDestinations.ManageRefundSchedule)
                    }
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

            composable<AppDestinations.MergeSchedule>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/merge" }
                )
            ) {
                MergeScheduleScreen(
                    openStockDetail = openStockDetail,
                    openUrl = openUrl,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<AppDestinations.ManageRefundSchedule> {
                ManageRefundScheduleScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<AppDestinations.Feedback>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/feedback" }
                )
            ) {
                FeedbackScreen(
                    simpleSnackbar = simpleSnackbar,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<AppDestinations.Notification>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "truespac://app/notification" }
                )
            ) {
                NotificationScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

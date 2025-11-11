package com.trueedu.spac.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.BuildConfig
import com.trueedu.spac.LoginCallback
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.ui.common.DeleteAccountDialog
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.common.LogoutDialog
import com.trueedu.spac.data.user.LocalRemoteConfig
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar
import com.trueedu.spac.ui.profile.views.MoreTopBar
import com.trueedu.spac.ui.settings.views.SettingItem
import com.trueedu.spac.ui.settings.views.SettingLabel
import com.trueedu.spac.ui.settings.views.SettingSwitchItem

@Composable
fun MoreScreen(
    simpleSnackbar: SimpleSnackbar,
    vm: MoreViewModel = hiltViewModel(),
    gotoPlayStore: () -> Unit,
    openDartScreen: () -> Unit,
    openRefundSchedule: () -> Unit,
    openFeedback: () -> Unit,
    loginWithGoogle: (LoginCallback) -> Unit,
    openAdmin: () -> Unit = {},
) {
    val context = LocalContext.current
    val trueAnalytics = LocalTrueAnalytics.current
    val remoteConfig = LocalRemoteConfig.current
    var logoutDialogVisible by remember { mutableStateOf(false) }
    var deleteAccountDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MoreTopBar(
                vm.email(),
                vm.profileImageUrl(),
            ) {
                loginWithGoogle(null)
            }
        },
        contentWindowInsets =
            ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        if (vm.loading.value) {
            LoadingView()
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingLabel("버전", BuildConfig.VERSION_NAME, gotoPlayStore)

            SettingItem("전자 공시 보기", true) {
                trueAnalytics.enterView("profile__dart__click")
                openDartScreen()
            }

            SettingItem("스팩 청산 일정", true) {
                trueAnalytics.enterView("profile__refund_schedule__click")
                openRefundSchedule()
            }

            if (vm.loggedIn()) {
                SettingItem("오류나 제안 보내기", true) {
                    trueAnalytics.enterView("profile__feedback__click")
                    openFeedback()
                }

                SettingItem("로그아웃", true) {
                    trueAnalytics.enterView("profile__logout__click")
                    logoutDialogVisible = true
                }

                SettingItem("탈퇴 및 데이터 삭제", true) {
                    trueAnalytics.enterView("profile__withdraw__click")
                    deleteAccountDialogVisible = true
                }
            }

            // 디버그 모드에서만 광고 on/off 표시
            if (BuildConfig.DEBUG) {
                SettingSwitchItem(
                    text = "광고 on/off",
                    checked = remoteConfig.adVisible,
                    onCheckedChange = { isChecked ->
                        remoteConfig.updateAdVisible(isChecked)
                    }
                )
            }

            // 관리자 모드인 경우 관리자 설정 버튼 표시
            if (vm.isAdminMode()) {
                SettingLabel("관리자 설정", "Worker 관리", openAdmin)
            }
        }

        if (logoutDialogVisible) {
            LogoutDialog(
                onConfirm = {
                    logoutDialogVisible = false
                    vm.logout(
                        onSuccess = {
                            simpleSnackbar.normal("로그아웃 되었습니다")
                        },
                        onFail = {
                            simpleSnackbar.error("로그아웃에 실패했습니다. 다시 시도해 주세요")
                        }
                    )
                },
                onDismiss = {
                    logoutDialogVisible = false
                }
            )
        }

        if (deleteAccountDialogVisible) {
            DeleteAccountDialog(
                onConfirm = {
                    deleteAccountDialogVisible = false
                    vm.deleteAccount(
                        context = context,
                        onSuccess = {
                            simpleSnackbar.normal("계정이 삭제 되었습니다")
                        },
                        onFail = {
                            simpleSnackbar.error("다시 시도해 주세요")
                        },
                    )
                },
                onDismiss = {
                    deleteAccountDialogVisible = false
                }
            )
        }
    }
}


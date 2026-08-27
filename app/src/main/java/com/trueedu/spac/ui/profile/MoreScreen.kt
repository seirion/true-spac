package com.trueedu.spac.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.trueedu.spac.BuildConfig
import com.trueedu.spac.LoginCallback
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.data.user.LocalRemoteConfig
import com.trueedu.spac.repo.local.LocalTrueLocal
import com.trueedu.spac.ui.common.DashDividerHorizontal
import com.trueedu.spac.ui.common.DeleteAccountDialog
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.common.LogoutDialog
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar
import com.trueedu.spac.ui.home.views.DisclosurePoint
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
    openMergeSchedule: () -> Unit,
    openFeedback: () -> Unit,
    openChat: () -> Unit,
    loginWithGoogle: (LoginCallback) -> Unit,
    openNotification: () -> Unit = {},
    openAdmin: () -> Unit = {},
) {
    val context = LocalContext.current
    val trueAnalytics = LocalTrueAnalytics.current
    val remoteConfig = LocalRemoteConfig.current
    val local = LocalTrueLocal.current
    var logoutDialogVisible by remember { mutableStateOf(false) }
    var deleteAccountDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MoreTopBar(
                email = vm.email(),
                profileImageUrl = vm.profileImageUrl(),
                onClick = { loginWithGoogle(null) },
                openNotification = openNotification,
            )
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
            DartDisclosureItem(vm.hasDisclosures()) {
                trueAnalytics.enterView("profile__dart__click")
                openDartScreen()
            }

            SettingItem("스팩 청산 일정", true) {
                trueAnalytics.enterView("profile__refund_schedule__click")
                openRefundSchedule()
            }

            SettingItem("스팩 합병 일정", true) {
                trueAnalytics.enterView("profile__merge_schedule__click")
                openMergeSchedule()
            }

            SettingSwitchItem(
                text = "화면 항상 켜기",
                checked = local.keepScreenOn.value,
                onCheckedChange = { isChecked ->
                    local.keepScreenOn.value = isChecked
                }
            )

            SettingLabel("버전", BuildConfig.VERSION_NAME, gotoPlayStore)

            if (vm.loggedIn()) {
                SettingItem("AI 공시 도우미", true) {
                    trueAnalytics.enterView("profile__chat__click")
                    openChat()
                }

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

@Composable
private fun DartDisclosureItem(
    hasDisclosures: Boolean,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp)
            .height(56.dp)
    ) {
        Row {
            TrueText(
                s = "전자 공시 보기",
                fontSize = 16,
                color = MaterialTheme.colorScheme.primary,
            )
            if (hasDisclosures) {
                Column { DisclosurePoint() }
            }
        }
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Outlined.ChevronRight,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "next"
        )
    }
    DashDividerHorizontal()
}

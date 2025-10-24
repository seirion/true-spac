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
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.BuildConfig
import com.trueedu.spac.LoginCallback
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.ui.common.DeleteAccountDialog
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar
import com.trueedu.spac.ui.profile.views.ProfileTopBar
import com.trueedu.spac.ui.settings.views.SettingItem
import com.trueedu.spac.ui.settings.views.SettingLabel

@Composable
fun ProfileScreen(
    simpleSnackbar: SimpleSnackbar,
    vm: ProfileViewModel = hiltViewModel(),
    gotoPlayStore: () -> Unit,
    loginWithGoogle: (LoginCallback) -> Unit,
    openAdmin: () -> Unit = {},
) {
    val trueAnalytics = LocalTrueAnalytics.current
    var deleteAccountDialogVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ProfileTopBar(
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

            if (vm.loggedIn()) {
                SettingItem("탈퇴 및 데이터 삭제", true) {
                    trueAnalytics.enterView("profile__withdraw__click")
                    deleteAccountDialogVisible = true
                }
            }

            // 관리자 모드인 경우 관리자 설정 버튼 표시
            if (vm.isAdminMode()) {
                SettingLabel("관리자 설정", "Worker 관리", openAdmin)
            }
        }

        if (deleteAccountDialogVisible) {
            DeleteAccountDialog(
                onConfirm = {
                    vm.deleteAccount(
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

package com.trueedu.spac.ui.profile

import com.trueedu.spac.LoginCallback
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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.BuildConfig
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.profile.views.ProfileTopBar
import com.trueedu.spac.ui.settings.views.SettingLabel

@Composable
fun ProfileScreen(
    vm: ProfileViewModel = hiltViewModel(),
    gotoPlayStore: () -> Unit,
    loginWithGoogle: (LoginCallback) -> Unit,
    openAdmin: () -> Unit = {},
) {
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

            // 관리자 모드인 경우 관리자 설정 버튼 표시
            if (vm.isAdminMode()) {
                SettingLabel("관리자 설정", "Worker 관리", openAdmin)
            }
        }
    }
}

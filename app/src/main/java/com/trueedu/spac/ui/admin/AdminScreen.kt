package com.trueedu.spac.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.ui.admin.views.WorkerStatusView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    vm: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("관리자 설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        contentWindowInsets =
            ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Worker 실행 상태 표시 (관리자 모드인 경우)
            WorkerStatusView(
                isAdminMode = vm.isAdminMode(),
                lastMasterFileUpdate = vm.lastMasterFileUpdate.value,
                lastMasterFileUpdate2 = vm.lastMasterFileUpdate2.value,
                masterFileExecutionCount = vm.masterFileExecutionCount.value,
                lastPriceUpdate = vm.lastPriceUpdate.value,
                lastPriceUpdate2 = vm.lastPriceUpdate2.value,
                priceExecutionCount = vm.priceExecutionCount.value,
                onRefresh = { vm.refreshWorkerStats() },
                onReset = { vm.resetWorkerStats() },
                onTestPriceUpdate = { vm.manuallyTriggerPriceUpdate() },
                onRestartAlarm = { vm.restartStockPriceAlarm() },
                onTestMasterFileUpdate = { vm.manuallyTriggerMasterFileUpdate() },
                onRescheduleWorker = { vm.reschedulePeriodicSyncWorker() }
            )
        }
    }
}

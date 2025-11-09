package com.trueedu.spac.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.ui.admin.views.WorkerStatusView
import com.trueedu.spac.ui.components.TrueText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    vm: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    openManageRefundSchedule: () -> Unit = {},
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
            // 환급 일정 관리 버튼
            Button(
                onClick = openManageRefundSchedule,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                TrueText(
                    s = "환급 일정 관리",
                    fontSize = 16,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Worker 실행 상태 표시 (관리자 모드인 경우)
            WorkerStatusView(
                isAdminMode = vm.isAdminMode(),
                lastMasterFileUpdate = vm.lastMasterFileUpdate.value,
                lastMasterFileUpdate2 = vm.lastMasterFileUpdate2.value,
                masterFileExecutionCount = vm.masterFileExecutionCount.value,
                lastPriceUpdate = vm.lastPriceUpdate.value,
                lastPriceUpdate2 = vm.lastPriceUpdate2.value,
                priceExecutionCount = vm.priceExecutionCount.value,
                isAlarmScheduled = vm.isAlarmScheduled.value,
                canScheduleExactAlarms = vm.canScheduleExactAlarms.value,
                isBatteryOptimizationIgnored = vm.isBatteryOptimizationIgnored.value,
                onRefresh = { vm.refreshWorkerStats() },
                onReset = { vm.resetWorkerStats() },
                onTestPriceUpdate = { vm.manuallyTriggerPriceUpdate() },
                onRestartAlarm = { vm.restartStockPriceAlarm() },
                onTestMasterFileUpdate = { vm.manuallyTriggerMasterFileUpdate() },
                onRescheduleWorker = { vm.reschedulePeriodicSyncWorker() },
                onOpenAlarmPermission = { vm.openAlarmPermissionSettings() },
                onOpenBatteryOptimization = { vm.openBatteryOptimizationSettings() }
            )
        }
    }
}

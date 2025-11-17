package com.trueedu.spac.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
            // 청산 일정 관리 버튼
            Button(
                onClick = openManageRefundSchedule,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                TrueText(
                    s = "청산 일정 관리",
                    fontSize = 16,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // 미국 주식 마스터 파일 다운로드 버튼
            Button(
                onClick = { vm.downloadUsMasterFile() },
                enabled = !vm.isDownloadingUsMaster.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                TrueText(
                    s = if (vm.isDownloadingUsMaster.value) "다운로드 중..." else "미국 주식 마스터 다운로드",
                    fontSize = 16,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // 다운로드 상태 메시지
            if (vm.usMasterDownloadMessage.value.isNotEmpty()) {
                Text(
                    text = vm.usMasterDownloadMessage.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (vm.usMasterDownloadMessage.value.contains("완료"))
                        MaterialTheme.colorScheme.primary
                    else if (vm.usMasterDownloadMessage.value.contains("실패"))
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp)
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

            Spacer(modifier = Modifier.height(16.dp))

            // FCM 푸시 테스트 섹션
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📤 FCM 푸시 테스트",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "⚠️ Debug 빌드에서만 동작합니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // FCM 토큰 입력
                    OutlinedTextField(
                        value = vm.fcmToken.value,
                        onValueChange = { vm.updateFcmToken(it) },
                        label = { Text("FCM 토큰") },
                        placeholder = { Text("상대방의 FCM 토큰을 입력하세요") },
                        trailingIcon = {
                            IconButton(onClick = { vm.pasteFromClipboard() }) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "붙여넣기",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 푸시 제목 입력
                    OutlinedTextField(
                        value = vm.pushTitle.value,
                        onValueChange = { vm.updatePushTitle(it) },
                        label = { Text("제목") },
                        placeholder = { Text("푸시 알림 제목") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 푸시 내용 입력
                    OutlinedTextField(
                        value = vm.pushBody.value,
                        onValueChange = { vm.updatePushBody(it) },
                        label = { Text("내용") },
                        placeholder = { Text("푸시 알림 내용") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 딥링크 입력 (선택)
                    OutlinedTextField(
                        value = vm.pushDeepLink.value,
                        onValueChange = { vm.updatePushDeepLink(it) },
                        label = { Text("딥링크 (선택)") },
                        placeholder = { Text("truespac://app/home") },
                        supportingText = {
                            Text(
                                text = "home, following, more, refund, feedback",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 버튼 Row
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { vm.clearPushTestFields() },
                            modifier = Modifier.weight(1f),
                            enabled = !vm.isSendingPush.value
                        ) {
                            Text("초기화")
                        }

                        Spacer(modifier = Modifier.padding(4.dp))

                        Button(
                            onClick = { vm.sendTestPush() },
                            modifier = Modifier.weight(1f),
                            enabled = !vm.isSendingPush.value
                        ) {
                            Text(
                                text = if (vm.isSendingPush.value) "전송 중..." else "푸시 전송"
                            )
                        }
                    }

                    // 전송 결과 메시지
                    if (vm.pushResultMessage.value.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = vm.pushResultMessage.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    vm.pushResultMessage.value.contains("성공") ->
                                        MaterialTheme.colorScheme.primary
                                    vm.pushResultMessage.value.contains("실패") ||
                                    vm.pushResultMessage.value.contains("오류") ->
                                        MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { vm.copyResultMessageToClipboard() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "결과 복사",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

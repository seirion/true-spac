package com.trueedu.spac.ui.admin.views

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.trueedu.spac.ui.components.TrueText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val REFRESH_ANIMATION_DURATION_MS = 600L

/**
 * Worker 실행 상태를 표시하는 UI 컴포넌트
 * 앱이 종료된 후에도 Worker가 실행되었는지 확인 가능
 */
@Composable
fun WorkerStatusView(
    isAdminMode: Boolean,
    lastMasterFileUpdate: String,
    lastMasterFileUpdate2: String,
    masterFileExecutionCount: Int,
    lastPriceUpdate: String,
    lastPriceUpdate2: String,
    priceExecutionCount: Int,
    isAlarmScheduled: Boolean,
    canScheduleExactAlarms: Boolean = true,
    isBatteryOptimizationIgnored: Boolean = true,
    onRefresh: () -> Unit = {},
    onReset: () -> Unit = {},
    onTestPriceUpdate: () -> Unit = {},
    onRestartAlarm: () -> Unit = {},
    onTestMasterFileUpdate: () -> Unit = {},
    onRescheduleWorker: () -> Unit = {},
    onOpenAlarmPermission: () -> Unit = {},
    onOpenBatteryOptimization: () -> Unit = {}
) {
    // 관리자 모드가 아니면 아무것도 표시하지 않음
    if (!isAdminMode) {
        return
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 회전 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "refresh")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Column(modifier = Modifier.padding(16.dp)) {
        // 관리자 모드 표시
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFEBEE)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    TrueText(
                        s = "⚠️ 관리자 모드",
                        fontSize = 16,
                        color = Color(0xFFD32F2F),
                        maxLines = 1
                    )
                    TrueText(
                        s = "백그라운드 작업이 활성화되어 있습니다",
                        fontSize = 12,
                        color = Color(0xFF757575),
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = {
                        isRefreshing = true
                        onRefresh()
                        coroutineScope.launch {
                            delay(REFRESH_ANIMATION_DURATION_MS) // 애니메이션이 보이도록 약간의 지연
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "새로고침",
                        tint = Color(0xFFD32F2F),
                        modifier = if (isRefreshing) {
                            Modifier.rotate(rotation)
                        } else {
                            Modifier
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 알람 권한 경고 (권한이 없을 때만 표시)
        if (!canScheduleExactAlarms && !isAlarmScheduled) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TrueText(
                        s = "⚠️ 알람 권한 필요",
                        fontSize = 16,
                        color = Color(0xFFFF6F00),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TrueText(
                        s = "정확한 알람 권한이 없어서 시세 업데이트 알람을 설정할 수 없습니다.",
                        fontSize = 12,
                        color = Color(0xFF757575),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenAlarmPermission,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6F00)
                            )
                        ) {
                            TrueText(s = "권한 설정", fontSize = 14, maxLines = 1)
                        }
                        Button(
                            onClick = onRestartAlarm,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            TrueText(s = "알람 재시작", fontSize = 14, maxLines = 1)
                        }
                    }
                    TrueText(
                        s = "※ 권한이 없어도 '알람 재시작'을 누르면 부정확한 알람으로 작동합니다.",
                        fontSize = 10,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 2
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 배터리 최적화 경고 (제외되지 않았을 때만 표시)
        if (!isBatteryOptimizationIgnored) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TrueText(
                        s = "🔋 배터리 최적화 제외 권장",
                        fontSize = 16,
                        color = Color(0xFF1976D2),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TrueText(
                        s = "배터리 최적화가 활성화되어 있으면 백그라운드 작업이 제한될 수 있습니다.",
                        fontSize = 12,
                        color = Color(0xFF757575),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenBatteryOptimization,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        TrueText(s = "배터리 최적화 제외 설정", fontSize = 14, maxLines = 1)
                    }
                    TrueText(
                        s = "※ 백그라운드 작업의 안정적인 실행을 위해 배터리 최적화를 제외하는 것을 권장합니다.",
                        fontSize = 10,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 3
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 마스터 파일 업데이트 상태
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TrueText(
                    s = "마스터 파일 업데이트",
                    fontSize = 14,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))

                TrueText(
                    s = "최근 실행 기록",
                    fontSize = 12,
                    color = Color(0xFF757575),
                    maxLines = 1
                )
                InfoRow("1번째", lastMasterFileUpdate)
                InfoRow("2번째", lastMasterFileUpdate2)
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow("총 실행 횟수", "${masterFileExecutionCount}회")

                TrueText(
                    s = "• 15-20분 간격으로 자동 실행",
                    fontSize = 12,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 시세 업데이트 상태
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrueText(
                        s = "시세 업데이트",
                        fontSize = 14,
                        maxLines = 1
                    )
                    // 알람 상태 표시
                    TrueText(
                        s = if (isAlarmScheduled) "✅ 알람 활성" else "❌ 알람 중단",
                        fontSize = 12,
                        color = if (isAlarmScheduled) Color(0xFF4CAF50) else Color(0xFFFF5722),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                TrueText(
                    s = "최근 실행 기록",
                    fontSize = 12,
                    color = Color(0xFF757575),
                    maxLines = 1
                )
                InfoRow("1번째", lastPriceUpdate)
                InfoRow("2번째", lastPriceUpdate2)
                Spacer(modifier = Modifier.height(4.dp))
                InfoRow("총 실행 횟수", "${priceExecutionCount}회")

                TrueText(
                    s = "• 거래 시간 중 5분 간격으로 실행",
                    fontSize = 12,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 1
                )
            }
        }

        // 도움말
        TrueText(
            s = "이 정보는 앱이 종료된 후에도 백그라운드 작업이 정상적으로 실행되었는지 확인할 수 있습니다.",
            fontSize = 12,
            color = Color(0xFF757575),
            modifier = Modifier.padding(top = 16.dp),
            maxLines = 2
        )

        // 테스트 버튼들
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 마스터 파일 수동 실행 버튼
            Button(
                onClick = onTestMasterFileUpdate,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                TrueText(s = "마스터 실행", fontSize = 14, maxLines = 1)
            }

            // 시세 수동 실행 버튼
            Button(
                onClick = onTestPriceUpdate,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                TrueText(s = "시세 실행", fontSize = 14, maxLines = 1)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 알람 재시작 버튼
            Button(
                onClick = onRestartAlarm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                TrueText(s = "알람 재시작", fontSize = 14, maxLines = 1)
            }

            // Worker 재등록 버튼
            Button(
                onClick = onRescheduleWorker,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                TrueText(s = "Worker 재등록", fontSize = 14, maxLines = 1)
            }
        }

        // 통계 초기화 버튼
        Button(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF5722)
            )
        ) {
            TrueText(s = "통계 초기화", fontSize = 14, maxLines = 1)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        TrueText(
            s = label,
            fontSize = 12,
            color = Color(0xFF757575),
            maxLines = 1
        )
        TrueText(
            s = value,
            fontSize = 14,
            maxLines = 1
        )
    }
}

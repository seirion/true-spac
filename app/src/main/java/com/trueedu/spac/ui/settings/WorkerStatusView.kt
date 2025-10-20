package com.trueedu.spac.ui.settings

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Worker 실행 상태를 표시하는 UI 컴포넌트
 * 앱이 종료된 후에도 Worker가 실행되었는지 확인 가능
 */
@Composable
fun WorkerStatusView(
    isAdminMode: Boolean,
    lastMasterFileUpdate: String,
    masterFileExecutionCount: Int,
    lastPriceUpdate: String,
    priceExecutionCount: Int,
    onRefresh: () -> Unit = {},
    onReset: () -> Unit = {},
    onTestPriceUpdate: () -> Unit = {},
    onRestartAlarm: () -> Unit = {},
    onTestMasterFileUpdate: () -> Unit = {},
    onRescheduleWorker: () -> Unit = {}
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
                    Text(
                        text = "⚠️ 관리자 모드",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "백그라운드 작업이 활성화되어 있습니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
                IconButton(
                    onClick = {
                        isRefreshing = true
                        onRefresh()
                        coroutineScope.launch {
                            delay(600) // 애니메이션이 보이도록 약간의 지연
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

        // 마스터 파일 업데이트 상태
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "마스터 파일 업데이트",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                InfoRow("마지막 실행", lastMasterFileUpdate)
                InfoRow("총 실행 횟수", "${masterFileExecutionCount}회")

                Text(
                    text = "• 15-20분 간격으로 자동 실행",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 8.dp)
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
                Text(
                    text = "시세 업데이트",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                InfoRow("마지막 실행", lastPriceUpdate)
                InfoRow("총 실행 횟수", "${priceExecutionCount}회")

                Text(
                    text = "• 거래 시간 중 5분 간격으로 실행",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // 도움말
        Text(
            text = "이 정보는 앱이 종료된 후에도 백그라운드 작업이 정상적으로 실행되었는지 확인할 수 있습니다.",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF757575),
            modifier = Modifier.padding(top = 16.dp)
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
                Text("마스터 실행")
            }

            // 시세 수동 실행 버튼
            Button(
                onClick = onTestPriceUpdate,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("시세 실행")
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
                Text("알람 재시작")
            }

            // Worker 재등록 버튼
            Button(
                onClick = onRescheduleWorker,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                )
            ) {
                Text("Worker 재등록")
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
            Text("통계 초기화")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF757575)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

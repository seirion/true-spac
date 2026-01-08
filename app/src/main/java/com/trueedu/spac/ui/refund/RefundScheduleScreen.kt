package com.trueedu.spac.ui.refund

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.trueedu.spac.api.model.dto.firebase.RefundSchedule
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.components.bottomsheet.DraggableBottomSheet
import com.trueedu.spac.util.formatter.cashFormatter
import com.trueedu.spac.util.formatter.dateFormat

@Composable
fun RefundScheduleScreen(
    vm: RefundScheduleViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "청산 일정",
                onBack = onBack,
                actionIcon = Icons.Filled.Info,
                onAction = { showBottomSheet = true }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (vm.loading) {
                LoadingView()
            } else {
                RefundScheduleContent(
                    schedules = vm.schedules
                )
            }
        }
    }

    DraggableBottomSheet(
        showBottomSheet = showBottomSheet,
        onDismiss = { showBottomSheet = false }
    ) {
        RefundScheduleInfoContent()
    }
}

@Composable
private fun RefundScheduleContent(
    schedules: List<RefundSchedule>
) {
    if (schedules.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TrueText(
                s = "청산 일정이 없습니다",
                fontSize = 16,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(schedules) { schedule ->
                RefundScheduleItem(schedule = schedule)
            }
        }
    }
}

@Composable
private fun RefundScheduleInfoContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TrueText(
            s = "주의사항",
            fontSize = 18,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        TrueText(
            s = "※ 청산 가격은 오차가 발생할 수 있으므로 증권사에서 정확한 값을 확인하시기 바랍니다.",
            fontSize = 14,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = Int.MAX_VALUE,
        )
    }
}

@Composable
private fun RefundScheduleItem(
    schedule: RefundSchedule
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                TrueText(
                    s = "${schedule.nameKr} (${schedule.code})",
                    fontSize = 16,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                TrueText(
                    s = "입금일: ${dateFormat(schedule.date)}",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                schedule.refundAmount?.let { amount ->
                    Spacer(modifier = Modifier.height(2.dp))
                    val statusText = if (schedule.fixed) " (확정)" else " (예상)"
                    TrueText(
                        s = "분배금: ${cashFormatter.format(amount)}$statusText",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

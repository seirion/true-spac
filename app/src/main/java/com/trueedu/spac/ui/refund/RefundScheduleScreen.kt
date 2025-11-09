package com.trueedu.spac.ui.refund

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.api.model.dto.firebase.RefundSchedule
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.util.formatter.cashFormatter
import com.trueedu.spac.util.formatter.dateFormat

@Composable
fun RefundScheduleScreen(
    vm: RefundScheduleViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "청산 일정",
                onBack = onBack
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
                .padding(16.dp)
        ) {
            items(schedules) { schedule ->
                RefundScheduleItem(schedule = schedule)
            }
        }
    }
}

@Composable
private fun RefundScheduleItem(
    schedule: RefundSchedule
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        TrueText(
            s = "${schedule.nameKr} (${schedule.code})",
            fontSize = 16,
            color = MaterialTheme.colorScheme.onSurface
        )
        TrueText(
            s = "입금일: ${dateFormat(schedule.date)}",
            fontSize = 14,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        schedule.refundAmount?.let { amount ->
            TrueText(
                s = "분배금: ${cashFormatter.format(amount)}",
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}


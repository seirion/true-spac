package com.trueedu.spac.ui.merge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.components.bottomsheet.DraggableBottomSheet
import com.trueedu.spac.ui.merge.model.MergeSchedule
import com.trueedu.spac.util.formatter.dateFormat
import java.net.URLEncoder

private fun formatDateOrDash(s: String): String {
    if (s.isBlank()) return "-"
    return dateFormat(s)
}

private fun dartCompanySearchUrl(companyName: String): String {
    val encoded = URLEncoder.encode(companyName, Charsets.UTF_8.name())
    return "https://dart.fss.or.kr/dsab001/main.do?autoSearch=true&textCrpNM=$encoded"
}

private fun formatDateRangeOrDash(start: String, end: String): String {
    if (start.isBlank() && end.isBlank()) return "-"
    return "${formatDateOrDash(start)} ~ ${formatDateOrDash(end)}"
}

@Composable
fun MergeScheduleScreen(
    vm: MergeScheduleViewModel = hiltViewModel(),
    openStockDetail: (String, String, Int?) -> Unit,
    openUrl: (String) -> Unit,
    onBack: () -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "합병 일정",
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
                MergeScheduleContent(
                    schedules = vm.schedules,
                    openStockDetail = openStockDetail,
                    openUrl = openUrl,
                )
            }
        }
    }

    DraggableBottomSheet(
        showBottomSheet = showBottomSheet,
        onDismiss = { showBottomSheet = false }
    ) {
        MergeScheduleInfoContent()
    }
}

@Composable
private fun MergeScheduleContent(
    schedules: List<MergeSchedule>,
    openStockDetail: (String, String, Int?) -> Unit,
    openUrl: (String) -> Unit,
) {
    if (schedules.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            TrueText(
                s = "합병 일정이 없습니다",
                fontSize = 16,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(schedules) { schedule ->
                MergeScheduleItem(
                    schedule = schedule,
                    openStockDetail = openStockDetail,
                    openUrl = openUrl,
                )
            }
        }
    }
}

@Composable
private fun MergeScheduleInfoContent() {
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
            s = "※ 합병 일정은 변동될 수 있으며 실제 증권사 스케줄과 다소 차이가 날 수 있으니 공시 및 증권사 공지를 함께 확인하시기 바랍니다.",
            fontSize = 14,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = Int.MAX_VALUE,
        )
    }
}

@Composable
private fun MergeScheduleItem(
    schedule: MergeSchedule,
    openStockDetail: (String, String, Int?) -> Unit,
    openUrl: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // 현재 스케줄의 code를 stockId로 사용
                openStockDetail(schedule.code, schedule.nameKr, null)
            },
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
            Column(modifier = Modifier.weight(1f)) {
                TrueText(
                    s = "${schedule.nameKr} (${schedule.code})",
                    fontSize = 16,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                val target = schedule.target.trim()
                if (target.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TrueText(
                            s = "합병 대상: ",
                            fontSize = 14,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = Int.MAX_VALUE,
                        )
                        TrueText(
                            s = target,
                            fontSize = 14,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                openUrl(dartCompanySearchUrl(target))
                            },
                            maxLines = Int.MAX_VALUE,
                            style = TextStyle(textDecoration = TextDecoration.Underline),
                        )
                    }
                } else {
                    TrueText(
                        s = "합병 대상: -",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = Int.MAX_VALUE,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                TrueText(
                    s = "합병반대의사통지 접수기간: ${formatDateRangeOrDash(schedule.dissentNoticeStartDate, schedule.dissentNoticeEndDate)}",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = Int.MAX_VALUE,
                )
                Spacer(modifier = Modifier.height(2.dp))
                TrueText(
                    s = "주식매수청구권 행사기간: ${formatDateRangeOrDash(schedule.appraisalRightStartDate, schedule.appraisalRightEndDate)}",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = Int.MAX_VALUE,
                )
                Spacer(modifier = Modifier.height(2.dp))
                TrueText(
                    s = "매매거래 정지예정기간: ${formatDateRangeOrDash(schedule.tradingHaltStartDate, schedule.tradingHaltEndDate)}",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = Int.MAX_VALUE,
                )
                Spacer(modifier = Modifier.height(2.dp))
                TrueText(
                    s = "신주의 상장예정일: ${formatDateOrDash(schedule.newShareListingDate)}",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = Int.MAX_VALUE,
                )

                Spacer(modifier = Modifier.height(8.dp))

                val disclosureUrl = schedule.disclosureUrl.trim()
                if (disclosureUrl.isNotEmpty()) {
                    TrueText(
                        s = "전자 공시 보기",
                        fontSize = 14,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            openUrl(disclosureUrl)
                        },
                        maxLines = Int.MAX_VALUE,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                    )
                } else {
                    TrueText(
                        s = "전자 공시 보기: -",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = Int.MAX_VALUE,
                    )
                }
            }
        }
    }
}

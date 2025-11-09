package com.trueedu.spac.ui.refund

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.api.model.dto.firebase.RefundSchedule
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.util.formatter.cashFormatter
import com.trueedu.spac.util.formatter.dateFormat
import kotlinx.coroutines.delay

@Composable
fun ManageRefundScheduleScreen(
    vm: ManageRefundScheduleViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.saveSuccess) {
        if (vm.saveSuccess) {
            snackbarHostState.showSnackbar("저장되었습니다")
            vm.clearMessages()
        }
    }

    LaunchedEffect(vm.errorMessage) {
        vm.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "청산 일정 관리",
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (vm.loading && vm.schedules.isEmpty()) {
                LoadingView()
            } else {
                ManageRefundScheduleContent(
                    vm = vm
                )
            }
        }
    }
}

@Composable
private fun ManageRefundScheduleContent(
    vm: ManageRefundScheduleViewModel
) {
    val listState = rememberLazyListState()

    LaunchedEffect(vm.scrollToTop) {
        if (vm.scrollToTop) {
            listState.animateScrollToItem(0)
            vm.resetScrollToTop()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AddScheduleForm(vm)
        }

        item {
            TrueText(
                s = "저장된 일정 (${vm.schedules.size})",
                fontSize = 18,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (vm.schedules.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TrueText(
                        s = "저장된 청산 일정이 없습니다",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(vm.schedules) { schedule ->
                ScheduleListItem(
                    schedule = schedule,
                    onEdit = { vm.startEdit(schedule) },
                    onDelete = { vm.deleteSchedule(schedule) }
                )
            }
        }
    }
}

@Composable
private fun AddScheduleForm(vm: ManageRefundScheduleViewModel) {
    // BackHandler로 suggestion 닫기
    BackHandler(enabled = vm.showSuggestions) {
        vm.hideSuggestions()
    }

    // Debounce를 위한 상태
    var searchQuery by remember { mutableStateOf("") }

    // ViewModel의 nameKr이 외부에서 변경될 때 동기화 (예: selectSuggestion, clearInputs 호출 시)
    LaunchedEffect(vm.nameKr) {
        if (searchQuery != vm.nameKr) {
            searchQuery = vm.nameKr
        }
    }

    // Debounce 처리: 300ms 후에 검색
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(300)
            vm.updateNameKr(searchQuery)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { vm.hideSuggestions() },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(
                        onClick = {},
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrueText(
                    s = if (vm.editingSchedule != null) "청산 일정 수정" else "새 청산 일정 추가",
                    fontSize = 16,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                vm.nameKr = it
                            },
                            label = { TrueText(s = "종목명 (한글)", fontSize = 14) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (vm.showSuggestions && vm.suggestions.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                shape = RoundedCornerShape(8.dp),
                                shadowElevation = 4.dp,
                                tonalElevation = 2.dp,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                LazyColumn {
                                    items(vm.suggestions) { stockInfo ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    vm.selectSuggestion(stockInfo)
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        ) {
                                            TrueText(
                                                s = stockInfo.nameKr,
                                                fontSize = 14,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            TrueText(
                                                s = stockInfo.code,
                                                fontSize = 12,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (stockInfo != vm.suggestions.last()) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = vm.code,
                    onValueChange = { vm.code = it.uppercase() },
                    label = { TrueText(s = "종목코드", fontSize = 14) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = vm.date,
                    onValueChange = { vm.date = it },
                    label = { TrueText(s = "입금일 (YYYYMMDD)", fontSize = 14) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { TrueText(s = "예: 20250315", fontSize = 14) }
                )

                OutlinedTextField(
                    value = vm.refundAmount,
                    onValueChange = { vm.refundAmount = it },
                    label = { TrueText(s = "분배금 (선택사항)", fontSize = 14) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    placeholder = { TrueText(s = "예: 1000", fontSize = 14) }
                )

                if (vm.editingSchedule != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { vm.cancelEdit() },
                            modifier = Modifier.weight(1f),
                            enabled = !vm.loading,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            TrueText(
                                s = "취소",
                                fontSize = 16,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { vm.addSchedule() },
                            modifier = Modifier.weight(1f),
                            enabled = !vm.loading,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            TrueText(
                                s = if (vm.loading) "수정 중..." else "수정",
                                fontSize = 16,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { vm.addSchedule() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !vm.loading,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        TrueText(
                            s = if (vm.loading) "저장 중..." else "추가",
                            fontSize = 16,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleListItem(
    schedule: RefundSchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                    TrueText(
                        s = "분배금: ${cashFormatter.format(amount)}",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "수정",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
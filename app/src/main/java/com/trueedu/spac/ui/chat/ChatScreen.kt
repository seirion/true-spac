package com.trueedu.spac.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.ui.chat.views.ChatDisclaimerPopupView
import com.trueedu.spac.ui.chat.views.MessageBubble
import com.trueedu.spac.ui.chat.views.WaitingIndicator
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.common.keyboardOverlapPadding
import com.trueedu.spac.ui.components.TouchIcon24
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar
import kotlinx.coroutines.delay

private const val REPLY_DELAY_NOTICE_MS = 30_000L
private const val WAITING_ITEM_KEY = "waiting"

@Composable
fun ChatScreen(
    simpleSnackbar: SimpleSnackbar,
    vm: ChatViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val trueAnalytics = LocalTrueAnalytics.current
    val listState = rememberLazyListState()

    // 답변이 오래 걸리면 "지연되고 있습니다"를 덧붙인다. 답변을 만드는 쪽이
    // 꺼져 있으면 영원히 안 오므로, 무한정 "생각 중"만 띄워두지 않는다.
    var delayed by remember { mutableStateOf(false) }
    LaunchedEffect(vm.isWaiting, vm.messages.lastOrNull()?.id) {
        delayed = false
        if (vm.isWaiting) {
            delay(REPLY_DELAY_NOTICE_MS)
            delayed = true
        }
    }

    // 키보드가 올라오면 목록 높이가 줄어드는데, LazyColumn 은 스크롤 위치를
    // 그대로 유지해서 마지막 대화가 가려진다. 열림/닫힘에만 반응한다 —
    // 높이 자체를 키로 쓰면 애니메이션 프레임마다 스크롤이 걸린다.
    val imeOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    // 새 메시지가 붙거나 답변이 채워지면 항상 마지막을 보여준다.
    LaunchedEffect(vm.messages.size, vm.messages.lastOrNull()?.text, vm.isUnclaimed, imeOpen) {
        val lastIndex = vm.messages.lastIndex + if (vm.isUnclaimed) 1 else 0
        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "AI 스팩 도우미 (BETA)",
                onBack = onBack,
                actionIcon = Icons.Default.DeleteOutline,
                onAction = {
                    trueAnalytics.clickButton("chat__clear__click")
                    vm.clear()
                },
            )
        },
        bottomBar = {
            InputBar(
                value = vm.input,
                enabled = !vm.isSending && !vm.isWaiting,
                onValueChange = { vm.input = it },
                onSend = {
                    trueAnalytics.clickButton("chat__send__click")
                    vm.send(
                        onFail = { simpleSnackbar.normal("전송에 실패했습니다. 다시 시도해주세요.") }
                    )
                },
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .keyboardOverlapPadding(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (vm.messages.isEmpty()) {
                EmptyGuide(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    items(vm.messages, key = { it.id }) { message ->
                        MessageBubble(message)
                    }

                    // 답변 말풍선이 아직 없는 구간을 메운다. pending 말풍선이
                    // 생긴 뒤로는 MessageBubble 이 "생각 중"을 맡는다.
                    if (vm.isUnclaimed) {
                        item(key = WAITING_ITEM_KEY) {
                            WaitingIndicator(delayed = delayed)
                        }
                    }
                }
            }
        }
    }

    if (vm.disclaimerVisible) {
        ChatDisclaimerPopupView(
            onConfirm = {
                trueAnalytics.clickButton("chat__disclaimer_confirm__click")
                vm.acceptDisclaimer()
            },
            onDismiss = {
                vm.dismissDisclaimer()
            },
        )
    }
}

@Composable
private fun EmptyGuide(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TrueText(
            s = "공시에 대해 물어보세요",
            fontSize = 16,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Margin(12)
        TrueText(
            s = "예) 삼성전자 최근 공시 알려줘\n" +
                "예) SK하이닉스 3개월간 중요한 공시만 요약해줘",
            fontSize = 14,
            color = MaterialTheme.colorScheme.surfaceVariant,
            maxLines = Int.MAX_VALUE,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InputBar(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                TrueText(
                    s = "무엇이든 물어보세요",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                )
            },
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
        )

        Margin(8)

        if (enabled && value.isNotBlank()) {
            TouchIcon24(icon = Icons.AutoMirrored.Filled.Send) { onSend() }
        } else {
            TouchIcon24(
                icon = Icons.AutoMirrored.Filled.Send,
                tint = MaterialTheme.colorScheme.outlineVariant,
            ) {}
        }
    }
}

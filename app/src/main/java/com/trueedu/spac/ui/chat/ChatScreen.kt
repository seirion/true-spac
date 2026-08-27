package com.trueedu.spac.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.ui.chat.views.MessageBubble
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.TouchIcon24
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar

@Composable
fun ChatScreen(
    simpleSnackbar: SimpleSnackbar,
    vm: ChatViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val trueAnalytics = LocalTrueAnalytics.current
    val listState = rememberLazyListState()

    // 새 메시지가 붙거나 답변이 채워지면 항상 마지막을 보여준다.
    LaunchedEffect(vm.messages.size, vm.messages.lastOrNull()?.text) {
        if (vm.messages.isNotEmpty()) {
            listState.animateScrollToItem(vm.messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "AI 공시 도우미",
                onBack = onBack,
                actionIcon = Icons.Default.DeleteOutline,
                onAction = {
                    trueAnalytics.clickButton("chat__clear__click")
                    vm.clear()
                },
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
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
                }
            }

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
        }
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
            .navigationBarsPadding()
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

package com.trueedu.spac.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.common.keyboardOverlapPadding
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar
import com.trueedu.spac.ui.edit.views.BottomBar

@Composable
fun FeedbackScreen(
    simpleSnackbar: SimpleSnackbar,
    vm: FeedbackViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val trueAnalytics = LocalTrueAnalytics.current

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "오류나 제안 보내기",
                onBack = onBack
            )
        },
        bottomBar = {
            BottomBar(
                text = "보내기",
                buttonEnabled = vm.title.isNotBlank() && vm.content.isNotBlank() && !vm.isSubmitting
            ) {
                trueAnalytics.clickButton("feedback__submit__click")
                vm.submitFeedback(
                    onSuccess = {
                        simpleSnackbar.normal("의견이 전송되었습니다. 감사합니다!")
                        onBack()
                    },
                    onFail = {
                        simpleSnackbar.normal("전송에 실패했습니다. 다시 시도해주세요.")
                    }
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .keyboardOverlapPadding(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Margin(16)

            // 제목 입력
            TrueText(
                s = "제목 *",
                fontSize = 14,
                color = MaterialTheme.colorScheme.onSurface
            )
            Margin(8)
            OutlinedTextField(
                value = vm.title,
                onValueChange = { vm.title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    TrueText(
                        s = "제목을 입력하세요",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                )
            )

            Margin(24)

            // 이메일 입력
            TrueText(
                s = "답장 받을 이메일 (선택)",
                fontSize = 14,
                color = MaterialTheme.colorScheme.onSurface
            )
            Margin(8)
            OutlinedTextField(
                value = vm.email,
                onValueChange = { vm.email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    TrueText(
                        s = "example@email.com",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                )
            )

            Margin(24)

            // 내용 입력
            TrueText(
                s = "내용 *",
                fontSize = 14,
                color = MaterialTheme.colorScheme.onSurface
            )
            Margin(8)
            OutlinedTextField(
                value = vm.content,
                onValueChange = { vm.content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                placeholder = {
                    TrueText(
                        s = "오류 내용이나 제안 사항을 자세히 작성해주세요",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
                maxLines = Int.MAX_VALUE
            )

            Margin(16)
        }
    }
}
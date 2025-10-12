package com.trueedu.spac.ui.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.AppNotice
import com.trueedu.spac.ui.components.TrueText

@Composable
fun NoticePopupView(
    notice: AppNotice,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    NoticeBody(
        notice.title,
        notice.body,
        onDismiss = onDismiss,
        onClickButton = onClick,
    )
}

@Composable
private fun NoticeBody(
    title: String,
    body: String,
    onDismiss: () -> Unit,
    onClickButton: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            TrueText(
                s = title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 32.dp, start = 20.dp, end = 20.dp)
                    .wrapContentHeight()
                    .fillMaxWidth(),
                maxLines = Int.MAX_VALUE,
            )
        },
        text = {
            TrueText(
                s = body,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14,
                textAlign = TextAlign.Center,
                maxLines = Int.MAX_VALUE,
                modifier = Modifier
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    .wrapContentHeight()
                    .fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onClickButton) {
                TrueText(
                    s = "확인",
                    fontSize = 17,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
        },
        containerColor = MaterialTheme.colorScheme.background,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewNoticePopupView() {
    NoticeBody(
        title = "Title",
        body = "공지 내용입니다",
        onDismiss = {},
        onClickButton = {},
    )
}

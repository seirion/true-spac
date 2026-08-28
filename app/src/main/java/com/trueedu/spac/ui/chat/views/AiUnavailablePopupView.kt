package com.trueedu.spac.ui.chat.views

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
import com.trueedu.spac.ui.components.TrueText

@Composable
fun AiUnavailablePopupView(
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onConfirm,
        title = {
            TrueText(
                s = "이용 불가 안내",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .wrapContentHeight()
                    .fillMaxWidth(),
                maxLines = Int.MAX_VALUE,
            )
        },
        text = {
            TrueText(
                s = "현재 AI 스팩 도우미 서비스를 일시적으로 이용할 수 없습니다.\n잠시 후 다시 시도해 주세요.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14,
                textAlign = TextAlign.Start,
                maxLines = Int.MAX_VALUE,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .wrapContentHeight()
                    .fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                TrueText(
                    s = "확인",
                    fontSize = 17,
                    fontWeight = FontWeight.W400,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = { },
        containerColor = MaterialTheme.colorScheme.background,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewAiUnavailablePopupView() {
    AiUnavailablePopupView(
        onConfirm = {},
    )
}

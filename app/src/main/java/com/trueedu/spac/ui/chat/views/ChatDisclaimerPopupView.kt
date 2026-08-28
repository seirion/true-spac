package com.trueedu.spac.ui.chat.views

import androidx.compose.foundation.layout.Column
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
fun ChatDisclaimerPopupView(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            TrueText(
                s = "AI 스팩 도우미 이용 안내",
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
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                TrueText(
                    s = """
                        이 기능은 AI가 스팩 정보와 전자공시(DART) 내용을 바탕으로 답합니다.

                        • AI는 실수나 오류가 섞인 답을 할 수 있습니다. 답변을 그대로 믿지 마시고, 실제 공시나 다른 자료로 반드시 교차 확인해 주세요.

                        • 투자 판단과 그 결과에 대한 책임은 전적으로 이용자 본인에게 있습니다.

                        • 이 기능은 현재 BETA 단계입니다. 응답 품질이 낮을 수 있고, 서비스가 예고 없이 중단될 수 있습니다.
                    """.trimIndent(),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14,
                    textAlign = TextAlign.Start,
                    maxLines = Int.MAX_VALUE,
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                )
            }
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
private fun PreviewChatDisclaimerPopupView() {
    ChatDisclaimerPopupView(
        onConfirm = {},
    )
}

package com.trueedu.spac.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
fun DisclaimerPopupView(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            TrueText(
                s = "안내 사항",
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
                // 한국어 문구
                TrueText(
                    s = """
                        본 앱은 투자 정보 제공을 목적으로 하며, 특정 종목의 매수·매도를 권유하거나 투자 수익을 보장하지 않습니다.

                        제공되는 데이터는 참고 자료로만 활용해 주시기 바라며, 실제 투자 시에는 반드시 증권사의 공식 데이터를 확인하시기 바랍니다.

                        모든 투자 판단과 그로 인한 손익은 전적으로 투자자 본인의 책임입니다.
                    """.trimIndent(),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 14,
                    textAlign = TextAlign.Start,
                    maxLines = Int.MAX_VALUE,
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                )

                // 구분선
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 20.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // 영어 문구
                TrueText(
                    s = """
                        This app is intended for informational purposes only and does not recommend buying or selling specific securities or guarantee investment returns.

                        The data provided should be used as reference material only. Please verify all information with your brokerage's official data before making any investment decisions.

                        You are solely responsible for all investment decisions and any resulting profits or losses.
                    """.trimIndent(),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    fontSize = 13,
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
private fun PreviewDisclaimerPopupView() {
    DisclaimerPopupView(
        onConfirm = {},
    )
}


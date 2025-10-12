package com.trueedu.spac.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.TrueText

@Preview(showBackground = true)
@Composable
fun ForceUpdateView(
    onClick: () -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
                    .copy(alpha = 0.9f)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .background(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.background
                )
                .padding(16.dp)
        ) {
            TrueText(
                s = "앱을 최신 버전으로 업데이트 해 주세요",
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.W600,
            )
            Margin(4)
            TextButton(
                onClick = onClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.errorContainer
                        .copy(alpha = 0.38f),
                    disabledContentColor = MaterialTheme.colorScheme.errorContainer
                        .copy(alpha = 0.38f),
                )
            ) {
                TrueText(
                    s = "업데이트 하기",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.W600,
                )
            }
        }
    }
}

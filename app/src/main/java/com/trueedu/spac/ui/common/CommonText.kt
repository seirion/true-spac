package com.trueedu.spac.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.trueedu.spac.ui.components.TrueText

@Composable
fun HeaderTitle(s: String) {
    TrueText(
        s = s,
        fontSize = 18,
        fontWeight = FontWeight.W600,
        maxLines = 1
    )
}

package com.trueedu.spac.ui.following.views

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun candleBgColor(): Color {
    return if (isSystemInDarkTheme()) {
        val factor = 0.4f
        MaterialTheme.colorScheme.surfaceVariant.let {
            Color(
                (it.red * factor).coerceAtMost(1f),
                (it.green * factor).coerceAtMost(1f),
                (it.blue * factor).coerceAtMost(1f),
            )
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant.let {
            val factor = 1.08f
            Color(
                (it.red * factor).coerceAtMost(1f),
                (it.green * factor).coerceAtMost(1f),
                (it.blue * factor).coerceAtMost(1f),
            )
        }
    }
}

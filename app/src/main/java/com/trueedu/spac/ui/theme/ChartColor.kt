package com.trueedu.spac.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ChartColor {
    val up: Color
        @Composable
        get() = if (isSystemInDarkTheme()) {
            Color(0xFFFF587A) // Dark mode up color
        } else {
            Color(0xFFFF2D55) // Light mode up color
        }

    val down: Color
        @Composable
        get() = if (isSystemInDarkTheme()) {
            Color(0xFF3398FF) // Dark mode down color
        } else {
            Color(0xFF007BFF) // Light mode down color
        }

    val neutral: Color
        @Composable
        get() = if (isSystemInDarkTheme()) {
            Color(0xFFA8B1BB) // Dark mode down color
        } else {
            Color(0xFF68727C) // Light mode down color
        }

    @Composable
    fun color(v: Double): Color {
        return when {
            v == 0.0 -> neutral
            v > 0 -> up
            else -> down
        }
    }
}

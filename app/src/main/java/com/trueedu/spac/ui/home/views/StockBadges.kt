package com.trueedu.spac.ui.home.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.trueedu.spac.ui.components.TrueText

@Composable
fun Badge(
    s: String = "정",
    bgColor: Color = Color.Red,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(12.dp)
            .background(color = bgColor)
    ) {
        TrueText(
            s = s,
            fontSize = 10,
            color = Color.White,
            lineHeight = 1.0.em,
        )
    }
}

@Composable
fun RoundedBadge(
    s: String = "보유",
    bgColor: Color = Color.Red,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(12.dp)
            .background(
                color = bgColor,
                shape = RoundedCornerShape(3.dp)
            )
            .padding(horizontal = 2.dp)
    ) {
        TrueText(
            s = s,
            fontSize = 10,
            color = Color.White,
            lineHeight = 1.0.em,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HaltBadge() {
    Badge("정", Color(0xFFBA1A1A))
}

@Preview(showBackground = true)
@Composable
fun DesignatedBadge() {
    Badge("관", Color(0xFFF57C00))
}

@Preview(showBackground = true)
@Composable
fun DisclosurePoint() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(4.dp)
            .background(
                color = Color(0xFF18DC43),
                shape = CircleShape,
            )
    ) {}
}

@Preview(showBackground = true)
@Composable
fun HoldingBadge(s: String = "123") {
    RoundedBadge(s, Color(0xFF448A33))
}
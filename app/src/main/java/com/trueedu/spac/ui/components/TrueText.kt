package com.trueedu.spac.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

@Composable
fun TrueText(
    s: String,
    fontSize: Int,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.W400,
    color: Color = MaterialTheme.colorScheme.primary,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = 1.2.em,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        modifier = modifier,
        text = s,
        fontWeight = fontWeight,
        color = color,
        fontSize = dpToSp(dp = fontSize.dp),
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        textAlign = textAlign,
        lineHeight = lineHeight,
        style = style,
    )
}

@Composable
fun TrueText(
    s: AnnotatedString,
    fontSize: Int,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.W400,
    color: Color = MaterialTheme.colorScheme.primary,
    maxLines: Int = 1,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = 1.2.em,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        modifier = modifier,
        text = s,
        fontWeight = fontWeight,
        color = color,
        fontSize = dpToSp(dp = fontSize.dp),
        overflow = TextOverflow.Ellipsis,
        maxLines = maxLines,
        textAlign = textAlign,
        lineHeight = lineHeight,
        style = style,
    )
}

@Composable
fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }

@Composable
fun Int.toPx(): Float {
    val dp = this.dp
    return with(LocalDensity.current) { dp.toPx() }
}


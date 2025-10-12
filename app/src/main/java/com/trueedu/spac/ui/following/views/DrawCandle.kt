package com.trueedu.spac.ui.following.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.theme.ChartColor

@Composable
fun DrawCandle(
    prevClose: Double,
    open: Double,
    high: Double,
    low: Double,
    close: Double,
) {
    // 값이 0으로 오는 경우가 있어서 값 보정
    val open = if (open == 0.0) prevClose else open
    val high = if (high == 0.0) prevClose else high
    val low = if (low == 0.0) prevClose else low

    val color = ChartColor.color(close - open)
    val bgColor = candleBgColor()
    Canvas(
        modifier = Modifier.width(16.dp)
            .fillMaxHeight()
    ) {
        drawCandle(
            canvasWidth = size.width,
            canvasHeight = size.height,
            prevClose = prevClose,
            open = open,
            high = high,
            low = low,
            close = close,
            color = color,
            bgColor = bgColor,
        )
    }
}

fun DrawScope.drawCandle(
    canvasWidth:Float,
    canvasHeight:Float,
    prevClose: Double,
    open: Double,
    high: Double,
    low: Double,
    close: Double,
    color: Color,
    bgColor: Color,
) {
    val candleWidth = canvasWidth * 0.8f

    // 가격을을 비율로 [0, 1] 스케일로 변환후 캔들 높이에 곱해줌
    val openY = normalize(prevClose, open) * canvasHeight
    val highY = normalize(prevClose, high) * canvasHeight
    val lowY = normalize(prevClose, low) * canvasHeight
    val closeY = normalize(prevClose, close) * canvasHeight

    drawRect(
        color = bgColor,
        topLeft = Offset(0f, 0f),
        size = Size(canvasWidth, canvasHeight),
    )

    // 캔들 몸통 그리기
    // open close 값이 같으면 아무것도 나오지 안아 약간 두께를 준다
    val bodyTop = minOf(openY, closeY)
    val bodyBottom = maxOf(openY, closeY)
    val bodyHeight = (bodyBottom - bodyTop).coerceAtLeast(1.dp.toPx())
    drawRect(
        color = color,
        topLeft = Offset((canvasWidth - candleWidth) / 2, bodyTop),
        size = Size(candleWidth, bodyHeight),
    )

    // 캔들 심지 그리기
    val strokeWidth = 2f
    drawLine(
        color = color,
        start = Offset((canvasWidth / 2) - (strokeWidth / 2), highY),
        end = Offset((canvasWidth / 2) - (strokeWidth / 2), lowY),
        strokeWidth = 2f // 캔들 심지 두께 설정
    )
}

/**
 * 가격을을 비율로 [0, 1] 스케일로 변환
 * prevClose 가 0.5 이며, 상한가인 +30% 이면 0, 하한가인 -30% 이면 1 이된다.
 * 30% 범위를 벗어나면 그냥 0 또는 1 로 변환한다
 */
private fun normalize(
    prevClose: Double,
    price: Double,
): Float {
    // y 축 상단이 0 이므로 1.0 에서 값을 빼 준다
    return 1f - ((price - prevClose * 0.7) / (0.6 * prevClose))
        .coerceAtLeast(0.0)
        .coerceAtMost(1.0)
        .toFloat()
}

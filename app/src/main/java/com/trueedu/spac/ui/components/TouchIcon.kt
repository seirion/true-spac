package com.trueedu.spac.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun TouchIcon24(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    TouchIconWithSize(24.dp, tint, icon, onClick)
}

@Composable
fun TouchIcon32(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    TouchIconWithSize(32.dp, tint, icon, onClick)
}

@Composable
private fun TouchIconWithSize(
    size: Dp,
    tint: Color,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val padding = 8.dp
    Box(
        modifier = Modifier
            .size(size + padding * 2)
            .clip(CircleShape)
            .clickable { onClick() },
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            imageVector = icon,
            tint = tint,
            contentDescription = "icon"
        )
    }
}

@Composable
fun TouchIconWithSizeRotating(
    size: Dp,
    tint: Color,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    var rotationTarget by remember { mutableIntStateOf(0) }
    val angle by animateFloatAsState(
        targetValue = rotationTarget.toFloat(),
        animationSpec = tween(durationMillis = 1000), // 1초 동안 회전
        finishedListener = { // 애니메이션 종료 시 호출
        }
    )

    val padding = 8.dp
    Box(
        modifier = Modifier
            .size(size + padding * 2)
            .clip(CircleShape)
            .graphicsLayer {
                rotationZ = 360f - (angle % 360f)
            }
            .clickable {
                if (rotationTarget % 360 == 0) {
                    rotationTarget += 360
                    onClick()
                }
            },
    ) {
        Icon(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            imageVector = icon,
            tint = tint,
            contentDescription = "icon"
        )
    }
}

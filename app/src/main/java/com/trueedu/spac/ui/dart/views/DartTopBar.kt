package com.trueedu.spac.ui.dart.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.common.CustomTopBar
import com.trueedu.spac.ui.components.TouchIcon32
import com.trueedu.spac.ui.components.TouchIconWithSizeRotating
import com.trueedu.spac.ui.components.TrueText

@Preview(showBackground = true)
@Composable
fun DartTopBar(
    num: Int = 0,
    onBack: () -> Unit = {},
    onRefresh: (() -> Unit)? = null,
) {
    CustomTopBar(
        navigationIcon = {
            TouchIcon32(
                icon = Icons.Filled.ChevronLeft,
                onClick = onBack,
            )
        },
        titleView = {
            TrueText(
                s = "오늘의 스팩 공시 $num",
                fontSize = 20,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actionsView = {
            if (onRefresh != null) {
                TouchIconWithSizeRotating(
                    size = 24.dp,
                    tint = MaterialTheme.colorScheme.primary,
                    icon = Icons.Outlined.Sync,
                    onClick = onRefresh,
                )
            }
        },
    )
}

package com.trueedu.spac.ui.dart.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.common.CustomTopBar
import com.trueedu.spac.ui.components.MySwitch
import com.trueedu.spac.ui.components.TouchIcon32
import com.trueedu.spac.ui.components.TouchIconWithSizeRotating
import com.trueedu.spac.ui.components.TrueText

@Preview(showBackground = true)
@Composable
fun DartTopBar(
    num: Int = 0,
    importantOnly: Boolean = false,
    importantCount: Int = 0,
    onBack: () -> Unit = {},
    onRefresh: (() -> Unit)? = null,
    onToggleImportant: () -> Unit = {},
) {
    CustomTopBar(
        navigationIcon = {
            TouchIcon32(
                icon = Icons.Filled.ChevronLeft,
                onClick = onBack,
            )
        },
        titleView = {
            val title = if (importantOnly && importantCount > 0) {
                "주요 공시 $importantCount"
            } else {
                "오늘의 스팩 공시 $num"
            }
            TrueText(
                s = title,
                fontSize = 20,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actionsView = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TrueText(
                    s = "주요",
                    fontSize = 12,
                    color = if (importantOnly)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 4.dp)
                )
                MySwitch(
                    checked = importantOnly,
                    onCheckedChange = { onToggleImportant() },
                )
                if (onRefresh != null) {
                    TouchIconWithSizeRotating(
                        size = 24.dp,
                        tint = MaterialTheme.colorScheme.primary,
                        icon = Icons.Outlined.Sync,
                        onClick = onRefresh,
                    )
                }
            }
        },
    )
}

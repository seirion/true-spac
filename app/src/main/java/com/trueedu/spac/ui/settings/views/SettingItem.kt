package com.trueedu.spac.ui.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.common.DashDividerHorizontal
import com.trueedu.spac.ui.components.TrueText

@Preview(showBackground = true)
@Composable
fun SettingItem(
    text: String = "나의 설정",
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    ItemWithIcon(text, Icons.Outlined.ChevronRight, enabled, onClick)
}

@Composable
fun ItemWithIcon(
    text: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp)
            .height(56.dp)
    ) {
        TrueText(
            s = text,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "next"
        )
    }
    DashDividerHorizontal()
}

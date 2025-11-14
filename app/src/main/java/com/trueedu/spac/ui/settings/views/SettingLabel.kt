package com.trueedu.spac.ui.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.common.DashDividerHorizontal
import com.trueedu.spac.ui.components.TrueText

@Composable
fun SettingLabel(
    title: String,
    value: String,
    onClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp)
            .height(56.dp)
    ) {
        TrueText(
            s = title,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
        TrueText(
            s = value,
            fontSize = 18,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
    DashDividerHorizontal()
}

@Preview(showBackground = true)
@Composable
private fun SettingLabelPreview() {
    SettingLabel("Version", "1.0.0")
}

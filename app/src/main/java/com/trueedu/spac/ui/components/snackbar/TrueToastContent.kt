package com.trueedu.spac.ui.components.snackbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.components.TrueText

@Composable
fun TrueToastContent(
    text: String?,
    icon: ImageVector? = null,
    iconTintColor: Color? = null,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = MaterialTheme.colorScheme.inverseSurface,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            val iconDescription = when {
                iconTintColor == MaterialTheme.colorScheme.error -> "오류"
                icon == Icons.Default.Info -> "정보"
                icon == Icons.Default.CheckCircle -> "완료"
                else -> "알림"
            }
            Icon(
                imageVector = icon,
                tint = iconTintColor ?: MaterialTheme.colorScheme.inverseOnSurface,
                contentDescription = iconDescription
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

        TrueText(
            s = text ?: "",
            fontSize = 16,
            fontWeight = FontWeight.W400,
            color = MaterialTheme.colorScheme.inverseOnSurface,
            modifier = Modifier.weight(1f),
            maxLines = 4,
        )
    }
}

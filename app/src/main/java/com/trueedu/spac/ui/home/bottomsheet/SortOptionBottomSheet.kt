package com.trueedu.spac.ui.home.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.components.bottomsheet.DraggableBottomSheet
import com.trueedu.spac.ui.home.model.SpacSort

@Composable
fun SortOptionBottomSheet(
    visible: Boolean,
    currentSelected: SpacSort,
    onDismiss: () -> Unit,
    onSelected: (SpacSort) -> Unit,
) {
    DraggableBottomSheet(
        showBottomSheet = visible,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            SortOptionBody(currentSelected, onSelected)
        }
    }
}

@Composable
fun ColumnScope.SortOptionBody(
    currentSelected: SpacSort,
    onSelected: (SpacSort) -> Unit,
) {
    TrueText(
        s = "정렬 방법",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20,
        fontWeight = FontWeight.W600,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    )
    SpacSort.entries.forEachIndexed { i, s ->
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onSelected(s)
                }
                .padding(vertical = 8.dp)
        ) {
            val icon = if (currentSelected == s) {
                Icons.Filled.CheckCircle
            } else {
                Icons.Outlined.CheckCircle
            }
            val iconColor = if (currentSelected == s) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            }
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = icon,
                tint = iconColor,
                contentDescription = "checked"
            )
            TrueText(
                s = s.title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16,
            )
        }
    }
}

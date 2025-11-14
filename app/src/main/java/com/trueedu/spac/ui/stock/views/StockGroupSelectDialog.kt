package com.trueedu.spac.ui.stock.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.trueedu.spac.ui.common.DashDividerHorizontal
import com.trueedu.spac.ui.common.DividerHorizontal
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.TrueText

enum class GroupSelectMode {
    ADD,    // 추가할 그룹 선택
    REMOVE  // 삭제할 그룹 선택
}

@Composable
fun StockGroupSelectDialog(
    stockName: String,
    mode: GroupSelectMode,
    groupNames: List<String?>,
    availableGroups: List<Int>, // 선택 가능한 그룹 인덱스 리스트
    onGroupSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(
                    MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            val title = when (mode) {
                GroupSelectMode.ADD -> "$stockName 추가할 그룹 선택"
                GroupSelectMode.REMOVE -> "$stockName 삭제할 그룹 선택"
            }

            TrueText(
                s = title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
            )
            Margin(8)
            DividerHorizontal()

            Column(modifier = Modifier.fillMaxWidth()) {
                availableGroups.forEach { groupIndex ->
                    val groupName = groupNames.getOrNull(groupIndex) ?: "관심 그룹 $groupIndex"
                    TrueText(
                        s = groupName,
                        fontSize = 16,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onGroupSelected(groupIndex)
                            }
                            .padding(vertical = 12.dp),
                    )
                    DashDividerHorizontal()
                }
            }

            Margin(8)
        }
    }
}


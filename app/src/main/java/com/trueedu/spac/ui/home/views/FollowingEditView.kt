package com.trueedu.spac.ui.home.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.components.dragDropColumn
import com.trueedu.spac.ui.components.swap

@Composable
fun FollowingEditView(
    page: Int,
    groupName: String,
    stocks: List<StockInfo>,
    onGroupNameChanged: (Int, String) -> Unit, // page, name
    onSave: (Int, List<String>) -> Unit, // page, stockIds
    onBack: () -> Unit,
) {
    var dirty by remember { mutableStateOf(false) }
    val items = remember {
        stocks.toMutableList()
    }

    Scaffold(
        topBar = {
            FollowingEditTopBar(
                title = groupName,
                onBack = onBack,
                onTitleChanged = {
                    onGroupNameChanged(page, it)
                }
            )
        },
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceDim,
    ) { innerPadding ->
        dragDropColumn(
            items = items,
            onSwap = { from, to ->
                logD("swap $from $to")
                swap(items, from, to)
                dirty = true
                true
            },
            onEditMoveCallback = { drop ->
                if (drop && dirty) onSave(page, items.map { it.code })
            },
            modifier = Modifier.padding(innerPadding),
            keyValue = { item, _ -> item.code },
        ) { stock, _ ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f),
                ) {
                    TrueText(
                        s = stock.nameKr,
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                    TrueText(
                        s = "(${stock.code})",
                        fontSize = 13,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                HandleIcon()
            }
        }
    }
}

@Composable
private fun HandleIcon() {
    Icon(
        imageVector = Icons.Filled.Reorder,
        contentDescription = "reorder",
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.secondary,
    )
}

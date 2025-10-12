package com.trueedu.spac.ui.home.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.common.CustomTopBar
import com.trueedu.spac.ui.components.TouchIcon24
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.home.model.SpacSort

@Preview(showBackground = true)
@Composable
internal fun HomeTopBar(
    sortType: SpacSort = SpacSort.ISSUE_DATE,
    onSortOption: () -> Unit = {},
    onFilterOption: () -> Unit = {},
) {
    CustomTopBar(
        navigationIcon = {},
        titleView = {
            TrueText(
                s = "스팩",
                fontSize = 20,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actionsView = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clip(shape = RoundedCornerShape(24.dp))
                    .clickable { onSortOption() }
                    .padding(16.dp, 10.dp, 4.dp, 10.dp)
            ) {
                TrueText(
                    s = sortType.title,
                    fontSize = 16,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Filled.ArrowDropDown,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "sort-select"
                )
            }
            TouchIcon24(icon = Icons.Filled.List, onClick = onFilterOption)
        }
    )
}

package com.trueedu.spac.ui.home.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.ui.components.TouchIcon32
import com.trueedu.spac.ui.components.TrueText

@Preview(showBackground = true)
@Composable
fun SearchBar(
    searchText: MutableState<String> = mutableStateOf(""),
    modifier: Modifier = Modifier,
    hint: String = "종목이름, 심볼",
    onSearch: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = searchText.value,
        onValueChange = { searchText.value = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { TrueText(hint, 14, color = MaterialTheme.colorScheme.surfaceVariant) },
        trailingIcon = {
            if (searchText.value.isEmpty()) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "검색"
                    )
                }
            } else {
                IconButton(onClick = { searchText.value = "" }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "지우기"
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearch(searchText.value)
            focusManager.clearFocus()
        }),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        )
    )
}

@Composable
fun SearchList(
    list: List<StockInfo>,
    itemChecked: (String) -> Boolean,
    toggleWatchList: (String) -> Unit,
    onItemClick: (StockInfo) -> Unit,
) {
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(list, key = { _, item -> item.code }) { _, item ->
            val checked = itemChecked(item.code)
            SearchStockItem(item, checked, toggleWatchList) {
                onItemClick(item)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchStockItem(
    item: StockInfo = StockInfoKospi("003456", "삼성전자", ""),
    checked: Boolean = true,
    toggleWatchList: (String) -> Unit = {},
    onClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .height(48.dp)
            .padding(horizontal = 16.dp)
    ) {
        val s = "${item.nameKr} (${item.code})"
        TrueText(
            s = s,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        val icon = if (checked) {
            Icons.Filled.Star
        } else {
            Icons.Outlined.Star
        }
        TouchIcon32(icon) {
            toggleWatchList(item.code)
        }
    }
}

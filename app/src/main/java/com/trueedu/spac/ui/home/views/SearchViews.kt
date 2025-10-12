package com.trueedu.spac.ui.home.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.ui.components.TouchIcon32
import com.trueedu.spac.ui.components.TrueText

@Composable
fun SearchBarWithSuggestions(
    searchText: MutableState<String>,
    searchHistory: List<String>,
    showSuggestions: Boolean,
    onSearch: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onHistoryClick: (String) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column {
            SearchBar(
                searchText = searchText,
                onSearch = onSearch,
                onFocusChanged = onFocusChanged
            )

            if (showSuggestions && searchHistory.isNotEmpty()) {
                SearchSuggestions(
                    searchHistory = searchHistory,
                    onHistoryClick = onHistoryClick,
                    onDeleteHistoryItem = onDeleteHistoryItem,
                    onClearHistory = onClearHistory
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBar(
    searchText: MutableState<String> = mutableStateOf(""),
    modifier: Modifier = Modifier,
    hint: String = "종목이름, 심볼",
    onSearch: (String) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = searchText.value,
        onValueChange = { searchText.value = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .onFocusChanged { onFocusChanged(it.isFocused) },
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

@Preview(showBackground = true)
@Composable
fun SearchSuggestions(
    searchHistory: List<String> = listOf("삼성전자", "SK하이닉스", "NAVER"),
    onHistoryClick: (String) -> Unit = {},
    onDeleteHistoryItem: (String) -> Unit = {},
    onClearHistory: () -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TrueText(
                    s = "최근 검색",
                    fontSize = 12,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier.height(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "검색 기록 삭제",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            searchHistory.forEach { query ->
                SearchHistoryItem(
                    query = query,
                    onClick = { onHistoryClick(query) },
                    onDelete = { onDeleteHistoryItem(query) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchHistoryItem(
    query: String = "삼성전자",
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp)
            )
            TrueText(
                s = query,
                fontSize = 14,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.height(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "삭제",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
            Icons.Filled.Favorite
        } else {
            Icons.Filled.FavoriteBorder
        }
        TouchIcon32(icon) {
            toggleWatchList(item.code)
        }
    }
}

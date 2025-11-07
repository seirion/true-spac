package com.trueedu.spac.ui.dart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.dart.model.DartListItem
import com.trueedu.spac.repo.local.LocalTrueLocal
import com.trueedu.spac.ui.common.DividerHorizontal
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.dart.views.DartTopBar
import com.trueedu.spac.data.log.logD

@Composable
fun DartScreen(
    vm: DartViewModel = hiltViewModel(),
    openUrl: (String) -> Unit,
    onBack: () -> Unit,
) {
    val local = LocalTrueLocal.current
    Scaffold(
        topBar = {
            val forceRefresh = if (local.dartApiKey.isNotEmpty()) {
                vm::forceRefresh
            } else {
                null
            }
            DartTopBar(vm.items.size, onBack, forceRefresh)
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) { innerPadding ->

        if (vm.loading) {
            LoadingView()
            return@Scaffold
        }

        if (vm.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                TrueText(
                    s = "오늘의 공시가 없습니다",
                    fontSize = 16,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            return@Scaffold
        }

        val state = rememberLazyListState()
        LazyColumn(
            state = state,
            modifier = Modifier.padding(innerPadding)
        ) {
            itemsIndexed(vm.items, key = { _, item -> item.first }) { _, (code, list) ->
                val stock = vm.getStock(code)
                if (stock == null) {
                    logD("Stock not found for code: $code")
                    return@itemsIndexed
                }
                NameView(stock.nameKr, stock.code)
                list.forEach {
                    DartListItemView(it) { receiptNum ->
                        val url = "https://dart.fss.or.kr/dsaf001/main.do?rcpNo=${receiptNum}"
                        openUrl(url)
                    }
                }
                Margin(4)
                DividerHorizontal()
            }
        }
    }
}

@Composable
private fun NameView(nameKr: String, code: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 8.dp)
    ) {
        TrueText(
            s = nameKr,
            fontSize = 14,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.W700,
        )
        Margin(4)
        TrueText(
            s = "(${code})",
            fontSize = 12,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun DartListItemView(
    item: DartListItem,
    onClick: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .clickable { onClick(item.receiptNum) }
            .padding(start = 24.dp, end = 8.dp)
    ) {
        TrueText(
            s = item.reportName,
            fontSize = 14,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Margin(8)
        TrueText(
            s = formatDate(item.receiptDate),
            fontSize = 12,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

private fun formatDate(date: String): String {
    // YYYYMMDD -> MM/DD
    return if (date.length == 8) {
        "${date.substring(4, 6)}/${date.substring(6, 8)}"
    } else {
        date
    }
}

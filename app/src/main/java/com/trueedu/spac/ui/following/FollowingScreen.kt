package com.trueedu.spac.ui.following

import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.following.views.FollowingEditPopupBody
import com.trueedu.spac.ui.following.views.FollowingItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull

@Composable
fun FollowingScreen(
    vm: FollowingViewModel = hiltViewModel(),
    openSearch: (Int) -> Unit = {}, // page
    openEdit: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        vm.init()
    }

    val pagerState = rememberPagerState(
        initialPage = 100 * vm.pageCount(),
        initialPageOffsetFraction = 0f,
        pageCount = { 200 * vm.pageCount() }, // infinite loop
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .mapNotNull { it.mod(vm.pageCount()) }
            .collectLatest {
                vm.currentPage.value = it
            }
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = vm.groupName(vm.currentPage.value),
                onBack = null,
                actionIcon = Icons.Filled.Search,
                onAction = { openSearch(pagerState.currentPage % vm.pageCount()) },
                actionIcon2 = Icons.Filled.Edit,
                onAction2 = openEdit,
            )
        },
        bottomBar = {
            /*
            if (
                !vm.loading.value &&
                vm.currentPage.value != null &&
                vm.getItems(vm.currentPage.value!!).isNotEmpty() &&
                remoteConfig.adVisible.value &&
                admobManager.nativeAd.value != null
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    NativeAdView(admobManager.nativeAd.value!!)
                }
            }
             */
        },
        contentWindowInsets =
            ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        val status by vm.stockPool.status.collectAsState()
        // 주식 정보와 관심 종목 정보를 모두 받아야 데이터 표시 가능
        if (vm.loading.value || status != StockPool.Status.SUCCESS) {
            LoadingView()
            return@Scaffold
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { position ->
            var selectedStock by remember { mutableStateOf<StockInfo?>(null) }
            var selectedStockIndex by remember { mutableIntStateOf(-1) }
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val items = vm.getItems(position % vm.pageCount())
                itemsIndexed(items, key = { _, item -> item }) { index, code ->
                    val stock = vm.getStock(code) ?: return@itemsIndexed
                    val basePrice = vm.basePrices[code]?.output

                    val price = basePrice?.price?.toDouble() ?: vm.prevPrice(code)
                    val delta = basePrice?.priceChange?.toDouble()
                    val rate = basePrice?.priceChangeRate?.toDouble()
                    val volume = basePrice?.volume?.toDouble() ?: 0.0

                    FollowingItem(
                        nameKr = stock.nameKr,
                        code = code,
                        price = price,
                        prevClose = basePrice?.previousClosePrice?.toDouble(),
                        open = basePrice?.open?.toDouble(),
                        high = basePrice?.high?.toDouble(),
                        low = basePrice?.low?.toDouble(),
                        delta = delta,
                        rate = rate,
                        volume = volume,
                        halt = stock.isHalt,
                        designated = stock.isDesignated,
                        hasDisclosure = vm.hasDisclosure(code),
                        onTradingClick = { /* TODO: gotoTrading(stock) */ },
                        onClick = { /* TODO: gotoStockDetail(stock) */ },
                    ) {
                        logD("long click: ${stock.nameKr}")
                        selectedStock = stock
                        selectedStockIndex = index
                    }
                }
            } // end of LazyColumn

            if (selectedStock != null) {
                Dialog(
                    onDismissRequest = { selectedStock = null },
                    properties = DialogProperties(usePlatformDefaultWidth = false) // Important for custom positioning
                ) {
                    FollowingEditPopupBody(
                        vm = vm,
                        item = selectedStock!!,
                        page = position,
                        index = selectedStockIndex,
                        moveTo = { index, toPage ->
                            selectedStock = null
                            vm.moveTo(index, toPage)
                        },
                        onRemove = {
                            selectedStock = null
                            vm.removeStock(selectedStockIndex)
                        },
                    )
                }
            }
        }
    }
}

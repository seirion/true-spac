package com.trueedu.spac.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.data.user.LocalRemoteConfig
import com.trueedu.spac.ui.ads.AdmobManager
import com.trueedu.spac.ui.ads.NativeAdView
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.home.bottomsheet.FilterOptionBottomSheet
import com.trueedu.spac.ui.home.bottomsheet.SortOptionBottomSheet
import com.trueedu.spac.ui.home.views.HomeTopBar
import com.trueedu.spac.ui.home.views.SearchBarWithSuggestions
import com.trueedu.spac.ui.home.views.SpacItem
import com.trueedu.spac.ui.home.views.SpacSectionView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    admobManager: AdmobManager,
    openStockDetail: (String, Int?) -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val remoteConfig = LocalRemoteConfig.current

    var sortOptionSheetVisible by remember { mutableStateOf(false) }
    var filterSheetVisible by remember { mutableStateOf(false) }

    val spacManager = vm.spacManager
    val loading by spacManager.loading.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(
                updateTimeStr = vm.priceUpdateTimeStr,
                sortType = vm.sort.value,
                onSortOption = {
                    if (!loading) {
                        sortOptionSheetVisible = true
                    }
                },
                onFilterOption = {
                    if (!loading) {
                        filterSheetVisible = true
                    }
                }
            )
        },
        bottomBar = {
            if (remoteConfig.adVisible && admobManager.nativeAd != null) {
                NativeAdView(admobManager.nativeAd!!)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentWindowInsets =
            ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
    ) { innerPadding ->
        if (loading) {
            LoadingView()
            return@Scaffold
        }

        val state = rememberLazyListState()

        LaunchedEffect(Unit) {
            state.scrollToItem(1)
        }

        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                SearchBarWithSuggestions(
                    searchText = vm.searchInput,
                    searchHistory = vm.searchHistory.value,
                    showSuggestions = vm.showSuggestions.value,
                    onSearch = { vm.onSearchSubmit() },
                    onFocusChanged = { vm.onSearchFocusChanged(it) },
                    onHistoryClick = { vm.onSearchHistoryClick(it) },
                    onDeleteHistoryItem = { vm.deleteSearchHistoryItem(it) },
                    onClearHistory = { vm.clearSearchHistory() }
                )
            }
            stickyHeader { SpacSectionView(vm::setSort) }

            itemsIndexed(vm.stocks.value, key = { i, _ -> i }) { i, item ->
                val spacRefund = spacManager.spacRefundMap.value[item.code]
                val redemptionValue = spacManager.getRedemptionValue(item.code)
                val expectedProfit = redemptionValue?.first
                val expectedProfitRate = redemptionValue?.second

                // 수동 보유 표시
                val holdingNum = vm.holdingNum(item.code)

                val hasDisclosure = vm.hasDisclosure(item.code)

                SpacItem(i, item,
                    spacRefund,
                    vm.price(item.code),
                    vm.priceChange(item.code),
                    vm.volume(item.code),
                    expectedProfit,
                    expectedProfitRate,
                    holdingNum,
                    hasDisclosure,
                ) {
                    openStockDetail(item.code, null)
                }
            }
        }

        SortOptionBottomSheet(
            visible = sortOptionSheetVisible,
            onDismiss = { sortOptionSheetVisible = false },
            currentSelected = vm.sort.value,
            onSelected = { option ->
                vm.setSort(option)
                sortOptionSheetVisible = false
            },
        )

        FilterOptionBottomSheet(
            visible = filterSheetVisible,
            current = vm.spacFilter,
            spacManager = vm.spacManager,
            onDismiss = { filterSheetVisible = false },
            onValueChanged = vm::updateFilter,
        )
    }
}

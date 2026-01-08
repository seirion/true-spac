package com.trueedu.spac.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
        val state = rememberLazyListState()
        // 화면이 재생성되어도 초기화 여부를 기억하기 위해 rememberSaveable 사용
        var initialScrollDone by rememberSaveable { mutableStateOf(false) }

        // 데이터가 로드되고 난 후, 최초 1회만 스크롤을 1번 위치(검색창 아래)로 이동
        LaunchedEffect(loading, vm.stocks.value.isNotEmpty()) {
            if (!loading && !initialScrollDone && vm.stocks.value.isNotEmpty()) {
                // 데이터가 있을 때만 스크롤 시도
                state.scrollToItem(1)
                initialScrollDone = true
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
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

                itemsIndexed(
                    vm.stocks.value,
                    key = { _, item -> item.code }
                ) { i, item ->
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

            if (loading) {
                LoadingView()
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

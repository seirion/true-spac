package com.trueedu.spac.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.home.views.HomeTopBar
import com.trueedu.spac.ui.home.views.SpacItem
import com.trueedu.spac.ui.home.views.SpacSectionView

@Composable
fun HomeScreen(
    vm: HomeViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            HomeTopBar(vm.sort.value)
        },
        bottomBar = {
            /*
            if (remoteConfig.adVisible.value && admobManager.nativeAd.value != null) {
                NativeAdView(admobManager.nativeAd.value!!)
            }
             */
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentWindowInsets =
            ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
    ) { innerPadding ->
        val spacManager = vm.spacManager
        val loading by spacManager.loading.collectAsState()
        if (loading) {
            LoadingView()
            return@Scaffold
        }

        val state = rememberLazyListState()

        LaunchedEffect(key1 = loading) {
            //state.scrollToItem(1)
        }

        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            //item { SearchBar(searchText = vm.searchInput) {} }
            stickyHeader { SpacSectionView(vm::setSort) }

            itemsIndexed(vm.stocks.value, key = { i, _ -> i }) { i, item ->
                val redemptionValue = spacManager.redemptionValueMap[item.code]
                val expectedProfit = redemptionValue?.first
                val expectedProfitRate = redemptionValue?.second

                // 한투 계좌 보유가 있으면 표시하고, 없으면 수동 보유를 표시함
                val holdingNum = 0.0

                // TODO: 공시 정보
                // val hasDisclosure = vm.hasDisclosure(item.code)

                SpacItem(i, item,
                    spacManager.priceMap[item.code] ?: 0.0,
                    spacManager.priceChangeMap[item.code],
                    spacManager.volumeMap[item.code] ?: 0L,
                    expectedProfit,
                    expectedProfitRate,
                    holdingNum,
                    false, // hasDisclosure,
                ) {
                    // TODO: stock detail
                }
            }
        }
    }
}

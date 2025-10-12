package com.trueedu.spac.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.home.views.SearchBar
import com.trueedu.spac.ui.home.views.SearchList

@Composable
fun SearchScreen(
    vm: SearchViewModel = hiltViewModel(),
    currentPage: Int,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        vm.setCurrentPage(currentPage)
    }

    Scaffold(
        topBar = { BackTitleTopBar("종목 검색", onBack) },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBar(searchText = vm.searchInput) {}
            SearchList(
                vm.searchResult.value,
                vm::inWatchList,
                vm::toggleWatchList,
                { /* TODO: stock detail */ },
            )
        }
    }
}

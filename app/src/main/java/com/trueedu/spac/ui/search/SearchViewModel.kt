package com.trueedu.spac.ui.search

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.data.stocks.FollowingManager
import com.trueedu.spac.data.stocks.SpacManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val trueAnalytics: TrueAnalytics,
    private val followingManager: FollowingManager,
    private val spacManager: SpacManager,
) : ViewModel() {

    val loading = mutableStateOf(true)

    // 관심종목 추가 시 사용되는 페이지 번호
    var targetPage: Int = 0
        private set

    val searchInput = mutableStateOf("")

    val searchResult = mutableStateOf<List<StockInfo>>(emptyList())

    init {

        viewModelScope.launch {
            launch {
                spacManager.loading
                    .collect {
                        if (!it) {
                            loading.value = false
                        }
                    }
            }

            launch {
                snapshotFlow { searchInput.value }
                    .debounce(300)
                    .collectLatest {
                        if (it.isEmpty()) {
                            searchResult.value = emptyList()
                        } else {
                            val result = spacManager.search(it)
                            searchResult.value = result
                        }
                    }
            }
        }
    }
    fun setCurrentPage(currentPage: Int) {
        targetPage = currentPage
    }

    fun inWatchList(code: String): Boolean {
        return followingManager.contains(targetPage, code)
    }

    fun toggleWatchList(code: String) {
        val currentOn = inWatchList(code)
        if (currentOn) {
            followingManager.remove(targetPage, code)
        } else {
            followingManager.add(targetPage, code)
        }
        trueAnalytics.clickToggleButton(
            "following__toggle__click",
            prevState = currentOn,
            mapOf("code" to code)
        )
    }
}

package com.trueedu.spac.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.data.stocks.SpacManager
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.ui.home.model.SpacFilter
import com.trueedu.spac.ui.home.model.SpacSort
import com.trueedu.spac.util.formatter.safeDouble
import com.trueedu.spac.util.formatter.safeLong
import com.trueedu.spac.util.toDateCompactString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userCycle: UserCycle,
    private val stockPool: StockPool,
    private val local: Local,
    val spacManager: SpacManager,
) : ViewModel() {

    val stocks = mutableStateOf<List<StockInfo>>(emptyList())

    val sort = mutableStateOf(SpacSort.ISSUE_DATE)

    var spacFilter = SpacFilter()

    val searchInput = mutableStateOf("")
    val searchHistory = mutableStateOf<List<String>>(emptyList())
    val showSuggestions = mutableStateOf(false)

    private val sortFun = mapOf<SpacSort, (StockInfo) -> Double>(
        SpacSort.ISSUE_DATE to { it.listingDate.safeLong().toDouble() },
        SpacSort.MARKET_CAP to { it.marketCap.safeLong().toDouble() },
        SpacSort.GROWTH_RATE to { -1 * growthRate(it) },
        SpacSort.REDEMPTION_VALUE to { -1 * (spacManager.redemptionValueMap[it.code]?.second ?: Double.MIN_VALUE) },
        SpacSort.VOLUME to { -1 * (spacManager.volumeMap[it.code]?.toDouble() ?: 0.0) },
    )

    init {
        loadSearchHistory()

        viewModelScope.launch {
            launch {
                spacManager.loading
                    .collect {
                        if (!it) {
                            // 초기값
                            stocks.value = spacManager.spacList.value
                                .filterNot { stockPool.delisted(it.code) }
                                .sortedBy(sortFun[sort.value]!!)
                        }
                    }
            }

            launch {
                snapshotFlow { searchInput.value }
                    .debounce(200)
                    .collectLatest {
                        filterStocks()
                    }
            }
        }
    }

    fun setSort(option: SpacSort) {
        sort.value = option
        filterStocks()
    }

    fun filterStocks() {
        stocks.value = spacManager.spacList.value
            .filterNot { stockPool.delisted(it.code) }
            .filter {
                if (spacFilter.listedOverTwoYears) {
                    val listingDate = it.listingDate.safeDouble()
                    val today = LocalDate.now().toDateCompactString().safeLong()
                    listingDate + 20000L < today
                } else {
                    true
                }
            }
            .filter {
                if (spacFilter.underParValue) {
                    val price = spacManager.priceMap.getOrDefault(it.code, 0.0).toInt()
                    val base = if (it.parValue.safeLong() == 100L) 2_000 else 10_000
                    price != 0 && price <= base
                } else {
                    true
                }
            }
            .filter {
                if (spacFilter.onlyWatching) {
                    false // TODO: watchList.contains(it.code)
                } else {
                    true
                }
            }
            .filter {
                val searchKey = searchInput.value.trim().lowercase()
                searchKey.isEmpty() ||
                        it.nameKr.lowercase().contains(searchKey)
            }
            .sortedBy(sortFun[sort.value]!!)
    }


    private fun growthRate(stock: StockInfo): Double {
        val code = stock.code
        val prevPrice = stock.prevPrice.safeDouble()
        val price = spacManager.priceMap.getOrDefault(code, prevPrice)
        val base = if (stock.parValue.safeLong() == 100L) 2_000 else 10_000
        return (price - base) * 100.0 / base
    }

    private fun loadSearchHistory() {
        searchHistory.value = local.getSearchHistory()
    }

    fun onSearchSubmit() {
        val query = searchInput.value.trim()
        if (query.isNotEmpty()) {
            local.addSearchHistory(query)
            loadSearchHistory()
            showSuggestions.value = false
        }
    }

    fun onSearchHistoryClick(query: String) {
        searchInput.value = query
        showSuggestions.value = false
        onSearchSubmit()
    }

    fun deleteSearchHistoryItem(query: String) {
        local.removeSearchHistory(query)
        loadSearchHistory()
        if (searchHistory.value.isEmpty()) {
            showSuggestions.value = false
        }
    }

    fun clearSearchHistory() {
        local.clearSearchHistory()
        loadSearchHistory()
    }

    fun onSearchFocusChanged(isFocused: Boolean) {
        showSuggestions.value = isFocused && searchHistory.value.isNotEmpty()
    }
}

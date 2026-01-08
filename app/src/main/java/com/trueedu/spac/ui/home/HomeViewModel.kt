package com.trueedu.spac.ui.home

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.stocks.DartManager
import com.trueedu.spac.data.stocks.FollowingManager
import com.trueedu.spac.data.stocks.PriceManager
import com.trueedu.spac.data.stocks.SpacManager
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.data.user.ManualAssets
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userCycle: UserCycle,
    private val stockPool: StockPool,
    private val local: Local,
    private val manualAssets: ManualAssets,
    val spacManager: SpacManager,
    private val dartManager: DartManager,
    private val priceManager: PriceManager,
    private val followingManager: FollowingManager,
) : ViewModel() {

    val stocks = mutableStateOf<List<StockInfo>>(emptyList())

    val sort = mutableStateOf(SpacSort.ISSUE_DATE)

    var spacFilter by mutableStateOf(SpacFilter())

    val searchInput = mutableStateOf("")
    val searchHistory = mutableStateOf<List<String>>(emptyList())
    val showSuggestions = mutableStateOf(false)

    private val sortFun = mapOf<SpacSort, (StockInfo) -> Double>(
        SpacSort.ISSUE_DATE to { it.listingDate.safeLong().toDouble() },
        SpacSort.MARKET_CAP to { it.marketCap.safeLong().toDouble() },
        SpacSort.GROWTH_RATE to { -1 * growthRate(it) },
        SpacSort.REDEMPTION_VALUE to { -1 * (spacManager.getRedemptionValue(it.code)?.second ?: Double.MIN_VALUE) },
        SpacSort.VOLUME to { -1.0 * (spacManager.getVolume(it.code, 0L)) },
    )

    init {
        dartManager.init()
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

            launch {
                snapshotFlow { manualAssets.assets.value }
                    .collectLatest {
                        if (it.none { asset -> asset.quantity > 0.0 } && spacFilter.onlyAssets) {
                            spacFilter = spacFilter.copy(onlyAssets = false)
                        }
                        filterStocks()
                    }
            }
        }
    }

    val priceUpdateTimeStr by derivedStateOf {
        val timestamp = priceManager.cacheTimestamp
        logD("priceUpdateTimeStr: $timestamp")
        if (timestamp == 0L) return@derivedStateOf "전일 종가"

        val timestampStr = timestamp.toString()
        if (timestampStr.length != 12) return@derivedStateOf "전일 종가"

        val dateStr = timestampStr.take(8)
        val timeStr = timestampStr.substring(8, 12)

        val cacheDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"))
        val today = LocalDate.now()

        if (cacheDate.isBefore(today)) {
            "전일 종가"
        } else {
            val time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HHmm"))
            val limitTime = LocalTime.of(15, 30)
            if (time.isAfter(limitTime)) {
                "15:30"
            } else {
                DateTimeFormatter.ofPattern("HH:mm").format(time)
            }
        }
    }

    val hasAssets = derivedStateOf {
        manualAssets.assets.value.any { it.quantity > 0.0 }
    }

    val myAssetCount = derivedStateOf {
        manualAssets.assets.value.count { it.quantity > 0.0 }
    }

    val myAssetTotalPrincipal = derivedStateOf {
        manualAssets.assets.value
            .asSequence()
            .filter { it.quantity > 0.0 }
            .sumOf { it.quantity * it.price }
    }

    val myAssetTotalValue = derivedStateOf {
        // PriceManager 내부 캐시는 Compose state가 아니므로, 타임스탬프를 읽어
        // 가격 업데이트 시 요약이 재계산되도록 트리거로 사용한다.
        priceManager.cacheTimestamp
        manualAssets.assets.value
            .asSequence()
            .filter { it.quantity > 0.0 }
            .sumOf { it.quantity * price(it.code) }
    }

    val myAssetProfitRate = derivedStateOf {
        val principal = myAssetTotalPrincipal.value
        if (principal <= 0.0) return@derivedStateOf 0.0
        val currentValue = myAssetTotalValue.value
        (currentValue - principal) * 100.0 / principal
    }

    fun setSort(option: SpacSort) {
        sort.value = option
        filterStocks()
    }

    fun updateFilter(newFilter: SpacFilter) {
        if (spacFilter != newFilter) {
            spacFilter = newFilter
            filterStocks()
        }
    }

    private fun matchesListingDateFilter(stock: StockInfo): Boolean {
        if (!spacFilter.listedOverTwoYears) return true
        val listingDate = stock.listingDate.safeDouble()
        val today = LocalDate.now().toDateCompactString().safeLong()
        return listingDate + 20000L < today
    }

    private fun matchesPriceFilter(stock: StockInfo): Boolean {
        if (!spacFilter.underParValue) return true
        val price = spacManager.getPrice(stock.code, 0.0).toInt()
        val base = if (stock.parValue.safeLong() == 100L) 2_000 else 10_000
        return price != 0 && price <= base
    }

    private fun matchesFollowingFilter(stock: StockInfo): Boolean {
        if (!spacFilter.filterFollowing) return true
        return followingManager.contains(stock.code)
    }

    private fun matchesSearchFilter(stock: StockInfo): Boolean {
        val searchKey = searchInput.value.trim().lowercase()
        return searchKey.isEmpty() || stock.nameKr.lowercase().contains(searchKey)
    }

    private fun matchesAssetFilter(stock: StockInfo): Boolean {
        if (!spacFilter.onlyAssets) return true
        return manualAssets.assets.value.any { it.code == stock.code && it.quantity > 0.0 }
    }

    fun filterStocks() {
        stocks.value = spacManager.spacList.value
            .filterNot { stockPool.delisted(it.code) }
            .filter { matchesListingDateFilter(it) }
            .filter { matchesPriceFilter(it) }
            .filter { matchesFollowingFilter(it) }
            .filter { matchesSearchFilter(it) }
            .filter { matchesAssetFilter(it) }
            .sortedBy(sortFun[sort.value]!!)
    }

    fun holdingNum(code: String): Double {
        return manualAssets.assets.value
            .firstOrNull { it.code == code }?.quantity ?: 0.0
    }

    fun hasDisclosure(code: String): Boolean {
        return dartManager.hasDisclosure(code)
    }

    fun price(code: String): Double =
        priceManager.price(code) ?: stockPool.get(code)?.prevPrice.safeDouble()

    fun priceChange(code: String): Double =
        priceManager.priceChange(code) ?: 0.0

    fun volume(code: String): Long =
        priceManager.volume(code) ?: stockPool.get(code)?.prevVolume.safeLong()

    private fun growthRate(stock: StockInfo): Double {
        val code = stock.code
        val prevPrice = stock.prevPrice.safeDouble()
        val price = spacManager.getPrice(code, prevPrice)
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

package com.trueedu.spac.ui.dart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.dart.model.DartListItem
import com.trueedu.spac.data.stocks.DartManager
import com.trueedu.spac.data.stocks.StockPool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DartViewModel @Inject constructor(
    private val trueAnalytics: TrueAnalytics,
    private val dartManager: DartManager,
    private val stockPool: StockPool,
) : ViewModel() {

    var loading by mutableStateOf(true)

    var items by mutableStateOf<List<Pair<String, List<DartListItem>>>>(emptyList())

    init {
        viewModelScope.launch {
            // StockPool이 SUCCESS가 될 때까지 대기
            stockPool.status
                .filter { it == StockPool.Status.SUCCESS }
                .first()

            // StockPool 준비 완료 후 dartManager 초기화
            dartManager.init()

            // 초기 데이터 로드
            loadItems()

            // dartManager 업데이트 구독
            dartManager.updateSignal.collect {
                loadItems()
            }
        }
    }

    private fun loadItems() {
        items = dartManager.getListMap().map { it.key to it.value }
        loading = false
    }

    fun forceRefresh() {
        trueAnalytics.clickButton("dart__force_refresh__click")
        dartManager.forceLoad()
    }

    fun getStock(code: String): StockInfo? {
        return stockPool.get(code)
    }
}

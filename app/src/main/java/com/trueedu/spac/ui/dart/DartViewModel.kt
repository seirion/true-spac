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
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.repo.local.Local
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DartViewModel @Inject constructor(
    private val userCycle: UserCycle,
    private val local: Local,
    private val trueAnalytics: TrueAnalytics,
    private val dartManager: DartManager,
    private val stockPool: StockPool,
) : ViewModel() {

    var loading by mutableStateOf(true)

    var items by mutableStateOf<List<Pair<String, List<DartListItem>>>>(emptyList())

    init {
        // dartManager 초기화
        dartManager.init()

        viewModelScope.launch {
            // 초기 데이터 로드
            items = dartManager.getListMap().map {
                it.key to it.value
            }
            loading = false
        }

        viewModelScope.launch {
            // 업데이트 구독 (별도 코루틴)
            dartManager.updateSignal.collectLatest {
                items = dartManager.getListMap().map {
                    it.key to it.value
                }
            }
        }
    }

    fun forceRefresh() {
        trueAnalytics.clickButton("dart__force_refresh__click")
        dartManager.forceLoad()
    }

    fun getStock(code: String): StockInfo? {
        return stockPool.get(code)
    }
}

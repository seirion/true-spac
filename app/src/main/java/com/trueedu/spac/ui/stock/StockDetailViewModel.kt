package com.trueedu.spac.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.SpacStatus
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.UserAsset
import com.trueedu.spac.data.stocks.DartManager
import com.trueedu.spac.data.stocks.FollowingManager
import com.trueedu.spac.data.stocks.PriceManager
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.data.user.ManualAssets
import com.trueedu.spac.repo.firebase.SpacStatusDatabase
import com.trueedu.spac.util.formatter.dateFormat
import com.trueedu.spac.util.formatter.numberFormatString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val trueAnalytics: TrueAnalytics,
    val followingManager: FollowingManager,
    val manualAssets: ManualAssets,
    private val stockPool: StockPool,
    private val spacStatusDatabase: SpacStatusDatabase,
    private val priceManager: PriceManager,
    val dartManager: DartManager,
) : ViewModel() {

    private val _spacStatus = MutableStateFlow<SpacStatus?>(null)
    val spacStatus: StateFlow<SpacStatus?> = _spacStatus.asStateFlow()

    private val _stockInfo = MutableStateFlow<StockInfo?>(null)
    val stockInfo: StateFlow<StockInfo?> = _stockInfo.asStateFlow()

    private val _infoList = MutableStateFlow<List<Pair<String, String?>>>(emptyList())
    val infoList: StateFlow<List<Pair<String, String?>>> = _infoList.asStateFlow()

    // 실시간 가격
    private val _currentPrice = MutableStateFlow<Double?>(null)
    val currentPrice: StateFlow<Double?> = _currentPrice.asStateFlow()

    // 가격 변동
    private val _priceChange = MutableStateFlow<Double?>(null)
    val priceChange: StateFlow<Double?> = _priceChange.asStateFlow()

    // 가격 변동률
    private val _priceChangeRate = MutableStateFlow<Double?>(null)
    val priceChangeRate: StateFlow<Double?> = _priceChangeRate.asStateFlow()

    // 가격 업데이트 수신 Job
    private var priceUpdateJob: Job? = null

    fun init(code: String) {
        _stockInfo.value = stockPool.get(code)
        initInfoList()

        // 실시간 가격 업데이트
        updatePriceInfo(code)

        viewModelScope.launch {
            val stockInfo = _stockInfo.value
            if (stockInfo?.isSpac == true) {
                try {
                    val list = spacStatusDatabase.load()
                    val spacStatus = list.firstOrNull { it.code == stockInfo.code }
                    _spacStatus.value = spacStatus
                } catch (e: Exception) {
                    // 로딩 실패 시 null로 유지
                    _spacStatus.value = null
                }
            }
        }

        // 기존 가격 업데이트 Job이 있으면 취소
        priceUpdateJob?.cancel()
        // PriceManager의 가격 업데이트를 수신
        priceUpdateJob = viewModelScope.launch {
            priceManager.priceUpdated.collect {
                updatePriceInfo(code)
            }
        }
    }

    private fun updatePriceInfo(code: String) {
        _currentPrice.value = priceManager.price(code)
        _priceChange.value = priceManager.priceChange(code)
        _priceChangeRate.value = priceManager.priceChangeRate(code)
    }

    fun isFollowing(): Boolean {
        val code = _stockInfo.value?.code ?: return false
        return followingManager.contains(code)
    }

    fun toggleFollowing() {
        val code = _stockInfo.value?.code ?: return
        if (followingManager.contains(code)) {
            // FIXME
            followingManager.remove(0, code)
        } else {
            followingManager.add(0, code)
        }
    }

    fun initInfoList() {
        val stockInfo = _stockInfo.value ?: return
        _infoList.value = listOf(
            "전일가격" to numberFormatString(stockInfo.prevPrice) + "원",
            "전일거래량" to numberFormatString(stockInfo.prevVolume),
            "시가총액" to numberFormatString(stockInfo.marketCap) + "억",
            "상장일자" to dateFormat(stockInfo.listingDate),
//            "상장주수" to numberFormatString(stockInfo.listingShares) + "K",
        )
    }

    fun getManualAsset(): UserAsset? {
        val code = _stockInfo.value?.code ?: return null
        return manualAssets.assets.value.firstOrNull { it.code == code }
    }
}

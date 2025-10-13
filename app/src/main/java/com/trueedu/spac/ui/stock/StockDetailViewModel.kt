package com.trueedu.spac.ui.stock

import androidx.lifecycle.ViewModel
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.price.PriceResponse
import com.trueedu.spac.data.stocks.FollowingManager
import com.trueedu.spac.data.stocks.SpacManager
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.util.formatter.dateFormat
import com.trueedu.spac.util.formatter.numberFormatString
import com.trueedu.spac.util.formatter.safeDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val trueAnalytics: TrueAnalytics,
    val followingManager: FollowingManager,
    private val spacManager: SpacManager,
    private val stockPool: StockPool,
) : ViewModel() {

    // 가격 정보 (api)
    private val _basePrice = MutableStateFlow<PriceResponse?>(null)
    val basePrice: StateFlow<PriceResponse?> = _basePrice.asStateFlow()

    private val _stockInfo = MutableStateFlow<StockInfo?>(null)
    val stockInfo: StateFlow<StockInfo?> = _stockInfo.asStateFlow()

    private val _infoList = MutableStateFlow<List<Pair<String, String?>>>(emptyList())
    val infoList: StateFlow<List<Pair<String, String?>>> = _infoList.asStateFlow()

    fun init(code: String) {
        _stockInfo.value = stockPool.get(code)
        initInfoList()
    }

    fun currentPrice(): Double {
        return _basePrice.value?.output?.price?.toDouble()
            ?: _stockInfo.value?.prevPrice.safeDouble()
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
        /*
        + if (stockInfo.isSpac) {
            assets.get(stockInfo.code)?.let {
                val priceQuantity = "${intFormatter.format(it.price)} • ${intFormatter.format(it.quantity)}주"
                val memo = if (it.memo.isNotEmpty()) "\n${it.memo}" else ""
                listOf("수동 입력 자산" to "${priceQuantity}${memo}")
            } ?: emptyList()
        }
         */
    }
}

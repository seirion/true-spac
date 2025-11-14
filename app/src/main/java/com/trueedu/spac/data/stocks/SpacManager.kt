package com.trueedu.spac.data.stocks

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.trueedu.spac.api.model.dto.firebase.SpacRefund
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.user.RemoteConfig
import com.trueedu.spac.repo.etc.readSpacRefund
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.util.formatter.safeDouble
import com.trueedu.spac.util.formatter.safeLong
import com.trueedu.spac.util.redemptionProfitRate
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SpacManager @Inject constructor(
    private val local: Local,
    private val stockPool: StockPool,
    private val remoteConfig: RemoteConfig,
    private val priceManagerProvider: Provider<PriceManager>,
) {
    val spacRefundMap = mutableStateOf<Map<String, SpacRefund>>(emptyMap())

    val loading = MutableStateFlow(true)
    val spacList = mutableStateOf<List<StockInfo>>(emptyList())
    val priceMap = mutableStateMapOf<String, Double>()
    val priceChangeMap = mutableStateMapOf<String, Double>()
    val volumeMap = mutableStateMapOf<String, Long>() // 거래량
    val volumePriceMap = mutableStateMapOf<String, Long>() // 거래대금
    val redemptionValueMap = mutableStateMapOf<String, Pair<Double, Double>>()

    val spacAnnualProfitMode = mutableStateOf(local.spacAnnualProfit)

    private var requestIndex = 0

    private val priceManager: PriceManager
        get() = priceManagerProvider.get()

    /**
     * PriceManager의 가격을 우선 사용하고, 유효하지 않으면 폴백 값 사용
     */
    private fun getPrice(code: String, fallback: Double): Double {
        return priceManager.price(code)?.takeIf { it > 0.0 } ?: fallback
    }

    /**
     * PriceManager의 거래량을 우선 사용하고, 유효하지 않으면 폴백 값 사용
     */
    private fun getVolume(code: String, fallback: Long): Long {
        return priceManager.volume(code)?.takeIf { it > 0 } ?: fallback
    }

    init {
        MainScope().launch {
            combine(
                stockPool.status.filter { it == StockPool.Status.SUCCESS },
                flow {
                    try {
                        emit(readSpacRefund())
                    } catch (e: Exception) {
                        logE("Failed to load spac refund", e)
                        emit(emptyMap())
                    }
                }
            ) { _, spacRefundMap -> spacRefundMap }
                .collect {
                    spacList.value = stockPool.search(StockInfo::isSpac)
                    spacRefundMap.value = it
                    init()
                }
        }
    }

    private fun init() {
        // 초기 값으로 전일 종가를 줌
        spacList.value.forEach {
            val fallbackPrice = it.prevPrice.safeDouble()
            val fallbackVolume = it.prevVolume.safeLong()
            priceMap[it.code] = getPrice(it.code, fallbackPrice)
            volumeMap[it.code] = getVolume(it.code, fallbackVolume)
            updateRedemptionValue(it.code)
        }

        loading.value = false
    }

    fun search(keyword: String): List<StockInfo> {
        val key = keyword.lowercase()
        return spacList.value.filter {
            it.code.lowercase().contains(key) || it.nameKr.lowercase().contains(key)
        }
    }

    fun setSpacAnnualProfit(on: Boolean) {
        local.spacAnnualProfit = on
        spacAnnualProfitMode.value = on

        // 재계산
        loading.value = true
        spacList.value.forEach {
            updateRedemptionValue(it.code)
        }
        loading.value = false
    }

    private fun updateRedemptionValue(code: String) {
        if (!remoteConfig.refundPriceVisible) {
            logD("Redemption value update skipped: refundPriceVisible is false")
            return
        }

        val stock = stockPool.get(code) ?: return
        val fallbackPrice = priceMap[code] ?: stock.prevPrice.safeDouble()
        val price = getPrice(code, fallbackPrice)
        val spacRefund = spacRefundMap.value[code] ?: return
        val redemptionPrice = spacRefund.settlementAmount() ?: return
        val targetDate = spacRefund.endDate
        val isAnnualized = spacAnnualProfitMode.value
        val (valueRate, valueRateAnnualized) = redemptionProfitRate(price, redemptionPrice, targetDate)
        val rate = if (isAnnualized) valueRateAnnualized else valueRate

        if (rate != null) {
            redemptionValueMap[code] = redemptionPrice to rate
        }
    }
}

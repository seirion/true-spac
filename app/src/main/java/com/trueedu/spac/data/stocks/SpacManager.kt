package com.trueedu.spac.data.stocks

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.trueedu.spac.api.model.dto.firebase.SpacStatus
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.repo.firebase.SpacStatusDatabase
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.util.formatter.safeDouble
import com.trueedu.spac.util.formatter.safeLong
import com.trueedu.spac.util.redemptionProfitRate
import com.trueedu.spac.util.toLocalDate
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpacManager @Inject constructor(
    private val local: Local,
    private val stockPool: StockPool,
    private val spacStatusDatabase: SpacStatusDatabase,
) {
    val spacStatusMap = mutableStateOf<Map<String, SpacStatus>>(emptyMap())

    val loading = MutableStateFlow(true)
    val spacList = mutableStateOf<List<StockInfo>>(emptyList())
    val priceMap = mutableStateMapOf<String, Double>()
    val priceChangeMap = mutableStateMapOf<String, Double>()
    val volumeMap = mutableStateMapOf<String, Long>() // 거래량
    val volumePriceMap = mutableStateMapOf<String, Long>() // 거래대금
    val redemptionValueMap = mutableStateMapOf<String, Pair<Int, Double>>()

    val spacAnnualProfitMode = mutableStateOf(local.spacAnnualProfit)

    private var requestIndex = 0

    init {
        MainScope().launch {
            combine(
                stockPool.status.filter { it == StockPool.Status.SUCCESS },
                flow {
                    try {
                        emit(spacStatusDatabase.load())
                    } catch (e: Exception) {
                        logE("Failed to load spac status", e)
                        emit(emptyList())
                    }
                }
            ) { _, spacStatuses -> spacStatuses }
                .collect {
                    spacList.value = stockPool.search(StockInfo::isSpac)
                    spacStatusMap.value = it
                        .associateBy(SpacStatus::code)
                    init()
                }
        }
    }

    private fun init() {
        // 초기 값으로 전일 종가를 줌
        spacList.value.forEach {
            priceMap[it.code] = it.prevPrice.safeDouble()
            volumeMap[it.code] = it.prevVolume.safeLong()
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
        val stock = stockPool.get(code) ?: return
        val price = priceMap[code] ?: stock.prevPrice.safeDouble()
        val redemptionPrice = spacStatusMap.value[code]?.redemptionPrice ?: return
        val listingDateStr = stockPool.get(code)?.listingDate ?: return
        val targetDate = listingDateStr.toLocalDate()!!
            .plusYears(3)
            .plusDays(-51)
        val isAnnualized = spacAnnualProfitMode.value
        val (valueRate, valueRateAnnualized) = redemptionProfitRate(price, redemptionPrice, targetDate)
        val rate = if (isAnnualized) valueRateAnnualized else valueRate

        if (rate != null) {
            redemptionValueMap[code] = redemptionPrice to rate
        }
    }
}

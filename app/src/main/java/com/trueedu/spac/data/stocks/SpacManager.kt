package com.trueedu.spac.data.stocks

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.util.formatter.safeDouble
import com.trueedu.spac.util.formatter.safeLong
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpacManager @Inject constructor(
    private val local: Local,
    private val stockPool: StockPool,
) {

    val loading = MutableStateFlow(true)
    val spacList = mutableStateOf<List<StockInfo>>(emptyList())
    val priceMap = mutableStateMapOf<String, Double>()
    val priceChangeMap = mutableStateMapOf<String, Double>()
    val volumeMap = mutableStateMapOf<String, Long>() // 거래량
    val volumePriceMap = mutableStateMapOf<String, Long>() // 거래대금
    val redemptionValueMap = mutableStateMapOf<String, Pair<Int, Double>>()

    // val spacAnnualProfitMode = mutableStateOf(local.spacAnnualProfit)

    private var requestIndex = 0

    init {
        MainScope().launch {
            stockPool.status.filter { it == StockPool.Status.SUCCESS }
                .collect {
                    spacList.value = stockPool.search(StockInfo::isSpac)
                    init()
                }
        }
    }

    private fun init() {
        // 초기 값으로 전일 종가를 줌
        spacList.value.forEach {
            priceMap[it.code] = it.prevPrice.safeDouble()
            volumeMap[it.code] = it.prevVolume.safeLong()
        }

        loading.value = false
    }
}

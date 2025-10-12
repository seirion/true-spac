package com.trueedu.spac.data.stocks

import com.trueedu.spac.api.model.dao.StockInfoLocal
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKosdaq
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.repo.firebase.FirebaseRealtimeDatabase
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.repo.local.StockLocal
import com.trueedu.spac.util.isHoliday
import com.trueedu.spac.util.toDateCompactString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockPool @Inject constructor(
    private val local: Local,
    private val stockLocal: StockLocal,
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase,
    private val stockInfoDownloader: StockInfoDownloader,
) {
    companion object {
        private val TAG = StockPool::class.java.simpleName
    }

    private var stocks: Map<String, StockInfo> = emptyMap()
    private var delisted: Set<String> = emptySet() // 상장 폐지 종목들

    enum class Status {
        LOADING,
        SUCCESS,
        FAIL,
        UPDATING,
    }
    val status = MutableStateFlow(Status.LOADING)

    /**
     * 종목 정보 로딩 전략:
     * 1. local database 에서 우선 종목 정보를 먼저 로딩한다
     * 2. 마스터파일 또는 realtime database 에 최신 데이터가 있으면 그것을 받아서
     *    local database 갱신
     *
     * 업데이트 우선순위:
     * 1순위: 마스터 파일 다운로드 (최신, 영업일에만) → Firebase에 업로드
     * 2순위: Firebase 리모트 데이터 → 로컬 DB에 저장
     * 3순위: 로컬 DB 데이터 → 메모리에 로드
     */
    fun loadStockInfo() {
        if (status.value == Status.SUCCESS || status.value == Status.UPDATING) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            delisted = loadDelistedStocks()
            val localStocks = loadLocalStocks()

            // 리모트 데이터가 필요한 지 체크
            val remoteUpdatedTime = firebaseRealtimeDatabase.lastUpdatedTime()
            val needUpdateRemote = needUpdateRemoteData(local.stockUpdatedAt, remoteUpdatedTime)
            val needUpdateMasterFile = needUpdateRemoteData(remoteUpdatedTime, currentTimeToYyyyMMddHHmm())

            logD("업데이트 체크 - 리모트(${needUpdateRemote}) 마스타파일(${needUpdateMasterFile})")
            val isWorkDay = !LocalDate.now().isHoliday()

            if (needUpdateMasterFile && isWorkDay) {
                // 1순위: 마스터 파일 다운로드 (가장 최신 데이터)
                logD("마스터 파일 업데이트 $remoteUpdatedTime < ${currentTimeToYyyyMMddHHmm()}")
                downloadMasterFiles()
            } else if (needUpdateRemote) {
                // 2순위: Firebase에서 데이터 가져오기
                logD("종목 업데이트 ${local.stockUpdatedAt} < $remoteUpdatedTime")
                val (_, stocks) = firebaseRealtimeDatabase.loadStocks()

                status.value = Status.SUCCESS
                this@StockPool.stocks = stocks
                local.stockUpdatedAt = remoteUpdatedTime
                logD("remote stocks(${stocks.size}) loaded")

                if (stocks.isNotEmpty()) {
                    writeToLocalDatabase(stocks.values)
                }
            } else {
                // 3순위: 로컬 DB 데이터 사용
                logD("종목 업데이트 불필요: ${localStocks.size}")
                status.value = Status.SUCCESS
                this@StockPool.stocks = localStocks
            }
        }
    }

    private suspend fun writeToLocalDatabase(stocks: Collection<StockInfo>) {
        val localStockInfoList = stocks.map {
            StockInfoLocal(it.code, it.nameKr, it.attributes, it.isKospi)
        }
        stockLocal.deleteAllStocks()
        stockLocal.setAllStocks(localStockInfoList)
    }

    private suspend fun loadDelistedStocks(): Set<String> {
        return firebaseRealtimeDatabase.loadDelistedStocks()
            .toSet()
    }

    /**
     * 로컬 데이터 베이스에서 종목 정보 가지고 오기
     */
    private suspend fun loadLocalStocks(): Map<String, StockInfo> {
        return stockLocal.getAllStocks()
            .map {
                if (it.kospi) {
                    StockInfoKospi(it.code, it.nameKr, it.attributes)
                } else {
                    StockInfoKosdaq(it.code, it.nameKr, it.attributes)
                }
            }
            .associateBy(StockInfo::code)
    }

    // 종목 파일을 다운로드/파싱하여 firebase 에 저장하기
    fun downloadMasterFiles() {
        status.value = Status.UPDATING
        CoroutineScope(Dispatchers.IO).launch {
            stocks = stockInfoDownloader.getStockInfoList()
                .associateBy(StockInfo::code)

            val yyyyMMddHHmm = currentTimeToYyyyMMddHHmm()
            firebaseRealtimeDatabase.writeStockInfo(yyyyMMddHHmm, stocks)

            if (stocks.isNotEmpty()) {
                local.stockUpdatedAt = yyyyMMddHHmm
                status.value = Status.SUCCESS
            } else {
                status.value = Status.FAIL
            }

            if (stocks.isNotEmpty()) {
                writeToLocalDatabase(stocks.values)
            }
        }
    }

    fun search(keyword: String): List<StockInfo> {
        val key = keyword.lowercase()
        return stocks.values.filter {
            // 일단 간단한 패턴 매칭
            it.code.lowercase().contains(key) || it.nameKr.lowercase().contains(keyword)
        }
    }

    fun search(predicate: (StockInfo) -> Boolean): List<StockInfo> {
        return stocks.values.filter(predicate)
    }

    /**
     * 분 단위 resolution
     */
    private fun currentTimeToYyyyMMddHHmm(): Long {
        return LocalDate.now().toDateCompactString().toLong()
    }

    fun get(code: String): StockInfo? {
        return stocks[code]
    }

    fun delisted(code: String): Boolean {
        return delisted.contains(code)
    }
}

package com.trueedu.spac.data.stocks

import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dao.StockInfoLocal
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKosdaq
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.repo.firebase.FirebaseRealtimeDatabase
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.repo.local.StockLocal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockPool @Inject constructor(
    private val local: Local,
    private val stockLocal: StockLocal,
    private val trueAnalytics: TrueAnalytics,
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase,
    private val stockInfoDownloader: StockInfoDownloader,
) {
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
     * 종목 정보 로딩 전략 (일반 사용자용):
     * 1. local database 에서 우선 종목 정보를 먼저 로딩한다
     * 2. 현재 시각 기준으로 업데이트가 필요하면 마스터 파일을 직접 다운로드하여 local database 갱신
     *
     * 업데이트 우선순위:
     * 1순위: 마스터 파일 직접 다운로드 → 로컬 DB에 저장
     * 2순위: 로컬 DB 데이터 → 메모리에 로드
     *
     * 주의: Firebase 업로드는 관리자 전용 기능 (PeriodicSyncWorker)
     */
    fun loadStockInfo() {
        if (status.value == Status.SUCCESS || status.value == Status.UPDATING) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            delisted = loadDelistedStocks()
            val localStocks = loadLocalStocks()

            // 현재 시각 기준으로 마스터 파일 업데이트 필요 여부 체크
            val currentTime = currentTimeToYyyyMMddHHmm()
            val needUpdate = needUpdateRemoteData(local.stockUpdatedAt, currentTime)

            logD("업데이트 체크 - 마스터 파일 직접 다운로드($needUpdate)")

            if (needUpdate) {
                logD("마스터 파일 다운로드 시작: localUpdatedAt=${local.stockUpdatedAt}, currentTime=$currentTime")
                try {
                    val stocksList = stockInfoDownloader.getKrStockInfoList()
                    if (stocksList.isNotEmpty()) {
                        stocks = stocksList.associateBy(StockInfo::code)
                        local.stockUpdatedAt = currentTime
                        writeToLocalDatabase(stocks.values)
                        logD("마스터 파일 다운로드 완료: ${stocks.size} stocks")
                    } else {
                        // 다운로드 결과 없을 시 로컬 데이터 사용
                        logD("마스터 파일 다운로드 결과 없음, 로컬 데이터 사용: ${localStocks.size}")
                        stocks = localStocks
                    }
                } catch (e: Exception) {
                    // 다운로드 실패 시 로컬 데이터 폴백
                    logD("마스터 파일 다운로드 실패, 로컬 데이터 사용: $e")
                    stocks = localStocks
                }
            } else {
                // 업데이트 불필요: 로컬 DB 데이터 사용
                logD("종목 업데이트 불필요: ${localStocks.size}")
                stocks = localStocks
            }

            status.value = if (stocks.isNotEmpty()) Status.SUCCESS else Status.FAIL
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

    /**
     * 마스터 파일 업데이트가 필요한지 확인
     * UpdateChecker의 needUpdateRemoteData() 로직을 사용
     * Firebase의 마지막 업데이트 이후 마스터 파일 업로드 시각이 있으면 업데이트 필요
     */
    suspend fun needUpdateMasterFile(): Boolean {
        val firebaseLastUpdated = firebaseRealtimeDatabase.lastUpdatedTime()
        val currentTime = currentTimeToYyyyMMddHHmm()

        // needUpdateRemoteData(local, remote) 로직 사용
        // Firebase가 local, 현재 시각이 remote
        return needUpdateRemoteData(firebaseLastUpdated, currentTime)
    }

    /**
     * 종목 파일을 다운로드/파싱하여 firebase 에 저장하기
     * 주의: 관리자 전용 기능 - 일반 사용자는 이 메서드를 직접 호출하지 않습니다
     */
    suspend fun downloadMasterFiles() {
        status.value = Status.UPDATING
        try {
            val stocksList = stockInfoDownloader.getKrStockInfoList()
            stocks = stocksList.associateBy(StockInfo::code)

            val yyyyMMddHHmm = currentTimeToYyyyMMddHHmm()
            firebaseRealtimeDatabase.writeStockInfo(yyyyMMddHHmm, stocks)

            if (stocks.isNotEmpty()) {
                local.stockUpdatedAt = yyyyMMddHHmm
                status.value = Status.SUCCESS
                logD("Master file downloaded and uploaded to Firebase: ${stocks.size} stocks")
                trueAnalytics.log(
                    "master_file_updated",
                    mapOf(
                        "stock_count" to stocks.size,
                        "updated_at" to yyyyMMddHHmm
                    )
                )
            } else {
                status.value = Status.FAIL
            }

            if (stocks.isNotEmpty()) {
                writeToLocalDatabase(stocks.values)
            }
        } catch (e: Exception) {
            logD("downloadMasterFiles failed: $e")
            status.value = Status.FAIL
            throw e
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
     * 현재 시각을 yyyyMMddHHmm 형식으로 반환
     * 분 단위 resolution
     */
    private fun currentTimeToYyyyMMddHHmm(): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
        return LocalDateTime.now().format(formatter).toLong()
    }

    fun get(code: String): StockInfo? {
        return stocks[code]
    }

    fun delisted(code: String): Boolean {
        return delisted.contains(code)
    }
}

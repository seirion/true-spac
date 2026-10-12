package com.trueedu.spac.data.stocks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.trueedu.spac.api.model.dao.StockPriceDao
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.user.TokenKeyManager
import com.trueedu.spac.di.ApplicationScope
import com.trueedu.spac.repo.firebase.FirebasePriceDatabase
import com.trueedu.spac.repo.kis.PriceRemote
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.util.formatter.safeDouble
import com.trueedu.spac.util.formatter.safeLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceManager @Inject constructor(
    private val local: Local,
    private val spacManager: SpacManager,
    private val priceRemote: PriceRemote,
    private val firebasePriceManager: FirebasePriceDatabase,
    private val tokenKeyManager: TokenKeyManager,
    private val stockPool: StockPool,
    @ApplicationScope private val applicationScope: CoroutineScope
) {
    companion object {
        // API 호출 제약: 1초당 최대 20회
        private const val MAX_API_CALLS_PER_SECOND = 20
        // 1초당 20회 제한을 준수하기 위해 여유를 두고 1.1초 대기
        private const val API_CALL_DELAY_MS = 1100L
        // Firebase에서 시세 데이터를 주기적으로 로드하는 간격 (10분)
        private const val PRICE_LOAD_INTERVAL_MS = 10 * 60 * 1000L
        // 백그라운드(알람/워커)에서 종목 목록이 준비되기를 기다리는 최대 시간
        private const val SPAC_READY_TIMEOUT_MS = 30_000L
        private const val SPAC_READY_POLL_MS = 200L
    }

    /** 시세 조회가 비어 있을 때의 사유 */
    enum class PriceFetchFailure {
        INVALID_USER_KEY,   // KIS UserKey 가 유효하지 않음
        NO_STOCKS,          // 종목 목록(spacList)이 비어 있어 조회할 대상이 없음
        ALL_FETCH_FAILED,   // 모든 종목의 시세 조회가 실패함
        EXCEPTION,          // 조회 중 예외
    }

    /**
     * 시세 조회 결과.
     * @param requested 조회를 시도한 종목 수 (일부만 성공했는지 판단하는 데 사용)
     * @param failure prices 가 비어 있을 때의 사유. 비어 있지 않으면 null
     */
    data class PriceFetchResult(
        val prices: Map<String, StockPriceDao>,
        val requested: Int,
        val failure: PriceFetchFailure? = null,
        val errorName: String? = null,
    )

    // 캐시된 시세 데이터
    private var cachedPriceMap: Map<String, StockPriceDao> = emptyMap()
    var cacheTimestamp by mutableStateOf(0L)

    // 가격 데이터 업데이트 알림용 Flow
    private val _priceUpdated = MutableStateFlow(0L)
    val priceUpdated: StateFlow<Long> = _priceUpdated.asStateFlow()

    // 주기적 업데이트를 위한 Job
    private var periodicLoadJob: Job? = null
    private var isStarted = false

    fun prevPrice(code: String): Double? {
        return cachedPriceMap[code]?.previousClosePrice?.safeDouble()
    }

    fun price(code: String): Double? {
        return cachedPriceMap[code]?.price?.safeDouble()
    }

    fun priceChange(code: String): Double? {
        return cachedPriceMap[code]?.priceChange?.safeDouble()
    }

    fun priceChangeRate(code: String): Double? {
        return cachedPriceMap[code]?.priceChangeRate?.safeDouble()
    }

    fun volume(code: String): Long? {
        return cachedPriceMap[code]?.volume?.safeLong()
    }

    fun open(code: String): Double? {
        return cachedPriceMap[code]?.open?.safeDouble()
    }

    fun high(code: String): Double? {
        return cachedPriceMap[code]?.high?.safeDouble()
    }

    fun low(code: String): Double? {
        return cachedPriceMap[code]?.low?.safeDouble()
    }

    /**
     * Firebase의 시세 데이터가 로컬보다 최신인지 확인
     * @return Firebase 쪽이 더 최신이면 true, 그렇지 않으면 false
     */
    suspend fun needToUpdatePrice(): Boolean {
        val localLastUpdatedAt = local.priceUpdatedAt
        val firebaseLastUpdatedAt = firebasePriceManager.lastUpdatedAt()

        return firebaseLastUpdatedAt > localLastUpdatedAt
    }

    /**
     * Firebase에서 시세 데이터 로드
     * @return Map<종목코드, StockPriceDao>
     */
    suspend fun loadPriceFromFirebase(): Map<String, StockPriceDao> {
        logD("loadPriceFromFirebase()")
        return try {
            val priceMap = firebasePriceManager.load()
            if (priceMap.isNotEmpty()) {
                val timestamp = firebasePriceManager.lastUpdatedAt()
                local.priceUpdatedAt = timestamp
                cachedPriceMap = priceMap
                cacheTimestamp = timestamp // Firebase timestamp와 동일한 형식 사용
                _priceUpdated.value = timestamp // UI 업데이트 트리거
                logD("loadPriceFromFirebase() - ${priceMap.size} prices loaded, timestamp: $timestamp")
            }
            priceMap
        } catch (e: Exception) {
            logE("Failed to load prices from Firebase", e)
            emptyMap()
        }
    }

    /**
     * 종목 목록(StockPool → SpacManager.spacList)이 준비될 때까지 기다린다.
     *
     * 종목 로딩([StockPool.loadStockInfo])은 앱이 포그라운드로 올라올 때만 호출되므로,
     * 알람/워커가 앱 프로세스를 새로 띄운 경우에는 종목 목록이 비어 있다.
     * 이 경우 직접 로딩을 시작하고 [timeoutMs] 안에 준비되는지 확인한다.
     *
     * @return 종목 목록이 준비되었으면 true
     */
    suspend fun ensureSpacListReady(timeoutMs: Long = SPAC_READY_TIMEOUT_MS): Boolean {
        if (spacManager.spacList.value.isNotEmpty()) return true

        logD("ensureSpacListReady() - 종목 목록이 비어 있어 로딩을 시작합니다 (status=${stockPool.status.value})")
        stockPool.loadStockInfo()
        val ready = withTimeoutOrNull(timeoutMs) {
            while (spacManager.spacList.value.isEmpty()) {
                if (stockPool.status.value == StockPool.Status.FAIL) return@withTimeoutOrNull false
                delay(SPAC_READY_POLL_MS)
            }
            true
        } ?: false
        logD("ensureSpacListReady() - ready=$ready, spacList=${spacManager.spacList.value.size}, status=${stockPool.status.value}")
        return ready
    }

    /**
     * 모든 SPAC 종목의 시세 정보를 조회하여 Map으로 반환
     * API 호출 제약: 1초당 최대 20회
     * @param forceRefresh true면 강제로 새로 조회, false면 캐시된 데이터 사용 가능
     * @return Map<종목코드, StockPriceDao>
     */
    suspend fun getPriceMap(
        forceRefresh: Boolean = false
    ): Map<String, StockPriceDao> = fetchPrices(forceRefresh).prices

    /**
     * [getPriceMap] 과 같지만 조회가 비었을 때의 사유와 조회 대상 수를 함께 돌려준다.
     * (워커가 실패 원인을 분석 이벤트로 남기기 위함)
     */
    suspend fun fetchPrices(
        forceRefresh: Boolean = false
    ): PriceFetchResult = coroutineScope {
        // UserKey 유효성 체크
        val userKey = tokenKeyManager.userKey.value
        if (userKey?.isValid() != true) {
            logE("❌ getPriceMap() - UserKey is not valid. Cannot fetch prices from API.")
            return@coroutineScope PriceFetchResult(emptyMap(), 0, PriceFetchFailure.INVALID_USER_KEY)
        }
        logD("✅ UserKey validated - proceeding with price fetch")

        // forceRefresh가 false이고 캐시가 있으면 캐시 반환
        if (!forceRefresh && cachedPriceMap.isNotEmpty()) {
            logD("getPriceMap() - returning cached data (${cachedPriceMap.size} items)")
            return@coroutineScope PriceFetchResult(cachedPriceMap, cachedPriceMap.size)
        }

        val stocks = spacManager.spacList.value
        logD("getPriceMap() - fetching prices for ${stocks.size} stocks (forceRefresh: $forceRefresh)")
        if (stocks.isEmpty()) {
            logE("❌ getPriceMap() - 종목 목록이 비어 있어 시세를 조회하지 않습니다 (StockPool status=${stockPool.status.value})")
            return@coroutineScope PriceFetchResult(emptyMap(), 0, PriceFetchFailure.NO_STOCKS)
        }

        try {
            val allPrices = mutableListOf<Pair<String, StockPriceDao>>()

            // API 제약: 1초에 최대 20회 호출
            // MAX_API_CALLS_PER_SECOND 개씩 묶어서 처리하고 각 chunk 사이에 1초 대기
            val chunks = stocks.chunked(MAX_API_CALLS_PER_SECOND)
            chunks.forEachIndexed { index, chunk ->
                logD("Fetching chunk ${index + 1}/${chunks.size} (${chunk.size} stocks)")

                // 현재 chunk의 모든 종목에 대해 병렬로 가격 조회
                val priceJobs = chunk.map { stock ->
                    async {
                        try {
                            val response = priceRemote.currentPrice(stock.code).first()
                            val output = response.output
                            if (output != null) {
                                val priceDao = StockPriceDao(
                                    nameKr = output.nameKr,
                                    price = output.price,
                                    priceChange = output.priceChange,
                                    priceChangeRate = output.priceChangeRate,
                                    volumePrice = output.volumePrice,
                                    volume = output.volume,
                                    open = output.open,
                                    high = output.high,
                                    low = output.low,
                                    previousClosePrice = output.previousClosePrice
                                )
                                stock.code to priceDao
                            } else {
                                logE("No price data for ${stock.code}")
                                null
                            }
                        } catch (e: Exception) {
                            logE("Failed to fetch price for ${stock.code}", e)
                            null
                        }
                    }
                }

                // 현재 chunk 완료 대기 (null 제외)
                val chunkPrices = priceJobs.awaitAll().filterNotNull()
                allPrices.addAll(chunkPrices)

                // 마지막 chunk가 아니면 API_CALL_DELAY_MS 대기 (API 제약 준수)
                if (index < chunks.size - 1) {
                    logD("Waiting ${API_CALL_DELAY_MS}ms before next chunk...")
                    delay(API_CALL_DELAY_MS)
                }
            }

            val priceMap = allPrices.toMap()
            if (priceMap.isEmpty()) {
                // 모든 조회가 실패한 경우에는 기존 캐시를 지우지 않는다
                logE("❌ getPriceMap() - 모든 종목(${stocks.size}개)의 시세 조회가 실패했습니다")
                return@coroutineScope PriceFetchResult(emptyMap(), stocks.size, PriceFetchFailure.ALL_FETCH_FAILED)
            }
            // 캐시 업데이트
            cachedPriceMap = priceMap
            // 현재 시간을 yyyyMMddHHmm 형식으로 저장
            cacheTimestamp = LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                .toLong()
            _priceUpdated.value = cacheTimestamp // UI 업데이트 트리거
            logD("getPriceMap() completed - ${priceMap.size}/${stocks.size} prices fetched, cache timestamp: $cacheTimestamp")
            PriceFetchResult(priceMap, stocks.size)
        } catch (e: Exception) {
            logE("Failed to get price map", e)
            PriceFetchResult(emptyMap(), stocks.size, PriceFetchFailure.EXCEPTION, e.javaClass.simpleName)
        }
    }


    /**
     * 시세 데이터를 Firebase에 저장
     * @param priceMap 저장할 시세 데이터 Map<종목코드, StockPriceDao>
     * @param customTimestamp 커스텀 타임스탬프 (null이면 현재 시각 사용)
     * @return 성공 시 true, 실패 시 false
     */
    suspend fun writePriceToFirebase(
        priceMap: Map<String, StockPriceDao>,
        customTimestamp: LocalDateTime? = null
    ): Boolean {
        logD("writePriceToFirebase() - writing ${priceMap.size} prices")
        return try {
            val success = firebasePriceManager.write(priceMap, customTimestamp)
            if (success) {
                // Firebase에 저장 성공 후 로컬 타임스탬프 업데이트
                val timestamp = firebasePriceManager.lastUpdatedAt()
                local.priceUpdatedAt = timestamp
                logD("writePriceToFirebase() completed successfully, timestamp: $timestamp")
            }
            success
        } catch (e: Exception) {
            logE("Failed to write prices to Firebase", e)
            false
        }
    }

    /**
     * 앱이 foreground로 전환될 때 호출
     * Firebase에서 5분마다 시세 데이터를 읽어 캐시에 저장
     */
    fun onStart() {
        if (isStarted) {
            logD("PriceManager.onStart() - already started, skipping")
            return
        }
        isStarted = true

        logD("PriceManager.onStart() - starting periodic price loading")

        periodicLoadJob = applicationScope.launch {
            // 즉시 한 번 실행
            try {
                // 캐시가 비어있거나 Firebase가 더 최신인 경우 로드
                val firebaseTimestamp = firebasePriceManager.lastUpdatedAt()
                val needsLoad = cachedPriceMap.isEmpty() ||
                    firebaseTimestamp > cacheTimestamp

                if (needsLoad) {
                    logD("Price update needed - loading from Firebase (cache empty: ${cachedPriceMap.isEmpty()}, firebase timestamp: $firebaseTimestamp, cache timestamp: $cacheTimestamp)")
                    loadPriceFromFirebase()
                } else {
                    logD("Price is up to date - skipping load (cache size: ${cachedPriceMap.size})")
                }
            } catch (e: Exception) {
                logE("Failed to check or load price on start", e)
            }

            while (isActive) {
                delay(PRICE_LOAD_INTERVAL_MS)

                // 업데이트 필요 여부 체크
                try {
                    val firebaseTimestamp = firebasePriceManager.lastUpdatedAt()
                    val needsLoad = cachedPriceMap.isEmpty() ||
                        firebaseTimestamp > cacheTimestamp

                    if (needsLoad) {
                        logD("Price update needed - loading from Firebase (periodic check)")
                        loadPriceFromFirebase()
                    } else {
                        logD("Price is up to date - skipping load")
                    }
                } catch (e: Exception) {
                    logE("Failed to check or load price during periodic update", e)
                }
            }
        }
    }

    /**
     * 앱이 background로 전환될 때 호출
     * 주기적 로딩 중지
     */
    fun onStop() {
        if (!isStarted) {
            logD("PriceManager.onStop() - not started, skipping")
            return
        }
        isStarted = false

        logD("PriceManager.onStop() - stopping periodic price loading")
        periodicLoadJob?.cancel()
        periodicLoadJob = null
    }
}

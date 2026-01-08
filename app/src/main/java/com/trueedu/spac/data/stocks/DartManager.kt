package com.trueedu.spac.data.stocks

import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.dart.model.DartListItem
import com.trueedu.spac.dart.model.DartListResponse
import com.trueedu.spac.dart.repository.remote.DartRemote
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.repo.etc.readDartCorpCode
import com.trueedu.spac.repo.firebase.FirebaseDartManager
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.util.isHoliday
import com.trueedu.spac.util.toDateTimeCompactString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DartManager @Inject constructor(
    private val local: Local,
    private val dartRemote: DartRemote,
    private val spacManager: SpacManager,
    private val firebaseDartManager: FirebaseDartManager,
    private val trueAnalytics: TrueAnalytics,
) {
    companion object {
        private const val CACHE_VALIDITY_MINUTES = 30L
        private val CACHE_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm")
    }

    private val items = ConcurrentHashMap<String, List<DartListItem>>()
    private var lastUpdatedAt = 0L // yyyyMMddHHmm (분단위)

    val updateSignal = MutableSharedFlow<Unit>()

    fun init() {
        logD("init() - ${local.dartApiKey.take(8)}")
        // TODO: MainScope 대신 명시적인 Job 관리나 ViewModel의 viewModelScope 사용 고려
        MainScope().launch(Dispatchers.IO) {
            // yyyyMMddHHmm
            val lastUpdatedAtRemote = firebaseDartManager.lastUpdatedAt()
            logD("lastUpdatedAtRemote: $lastUpdatedAtRemote")
            val now = LocalDateTime.now()
            val hasApiKey = local.dartApiKey.isNotBlank()

            val lastUpdatedAtRemoteTime = parseCacheTime(lastUpdatedAtRemote)
            val cacheAgeMinutes = if (lastUpdatedAtRemoteTime == null) {
                Long.MAX_VALUE
            } else {
                ChronoUnit.MINUTES.between(lastUpdatedAtRemoteTime, now).coerceAtLeast(0L)
            }

            if (hasApiKey && cacheAgeMinutes > CACHE_VALIDITY_MINUTES) {
                // 다시 로딩
                while (spacManager.loading.value) {
                    //logD("waiting spacManager")
                    delay(200)
                }
                val list = spacManager.spacList.value
                syncListToFirebase(list.map { it.code })
                logD("lastUpdatedAt: $lastUpdatedAt")
            } else {
                lastUpdatedAt = lastUpdatedAtRemote
                firebaseDartManager.loadDartList().forEach {
                    if (it.list?.isNotEmpty() == true) {
                        val code = it.list.first().stockCode
                        items[code] = it.list
                        updateSignal.emit(Unit)
                    }
                }
            }
            logD("init() completed - ${items.size}")
        }
    }

    private fun parseCacheTime(timestamp: Long): LocalDateTime? {
        if (timestamp <= 0L) return null
        val s = timestamp.toString()
        if (s.length != 12) return null
        return runCatching {
            LocalDateTime.parse(s, CACHE_TIME_FORMATTER)
        }.getOrNull()
    }

    fun getSize(): Int {
        return items.size
    }

    fun getListMap(): Map<String, List<DartListItem>> {
        return items
    }

    suspend fun CoroutineScope.syncListToFirebase(codes: List<String>): Int {
        if (local.dartApiKey.isBlank()) return 0

        // 기존 Firebase 데이터 로드
        val existingDartData = firebaseDartManager.loadDartList()
        val existingItemsMap = buildExistingItemsMap(existingDartData)

        // 이전 날짜 데이터 제거
        items.clear()

        val dartCorpMap = readDartCorpCode()
        val fromDate = latestWorkDay()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        // 새로운 공시 개수 및 실패한 종목 추적
        var totalNewDisclosuresCount = 0
        val failedStocks = mutableListOf<String>()

        // 모든 API 호출을 동시에 실행하고 결과를 기다림
        codes.map { code ->
            async {
                processStockDisclosure(code, dartCorpMap, fromDate, existingItemsMap, failedStocks)
            }
        }.awaitAll().forEach { count ->
            totalNewDisclosuresCount += count
        }

        lastUpdatedAt = LocalDateTime.now()
            .toDateTimeCompactString()
            .dropLast(2) // ss 제거 하여 yyyyMMddHHmm 으로 변환
            .toLong()

        // 실패한 종목이 있으면 로그 출력
        if (failedStocks.isNotEmpty()) {
            logE("⚠️ 공시 조회 실패한 종목 (${failedStocks.size}개): ${failedStocks.take(5).joinToString(", ")}${if (failedStocks.size > 5) " ..." else ""}")
        }

        // 완료 후 firebase 업데이트
        val success = firebaseDartManager.writeDartList(
            items.values.map {
                DartListResponse(status = "", message = "", list = it)
            }
        )

        if (success) {
            trueAnalytics.log(
                "dart__write_completed",
                mapOf(
                    "num" to items.size,
                    "newCount" to totalNewDisclosuresCount,
                    "failedCount" to failedStocks.size
                )
            )
        } else {
            trueAnalytics.log(
                "dart__write_failed",
                mapOf(
                    "num" to items.size,
                    "newCount" to totalNewDisclosuresCount,
                    "failedCount" to failedStocks.size
                )
            )
        }

        return totalNewDisclosuresCount
    }

    private fun buildExistingItemsMap(existingDartData: List<DartListResponse>): Map<String, List<DartListItem>> {
        return existingDartData
            .mapNotNull { response ->
                response.list?.let { list ->
                    if (list.isNotEmpty()) {
                        // 모든 항목이 같은 stockCode를 가지고 있는지 확인
                        val stockCodes = list.map { it.stockCode }.toSet()
                        if (stockCodes.size == 1) {
                            stockCodes.first() to list
                        } else {
                            logE("⚠️ 하나의 response에 여러 stockCode 존재: ${stockCodes.joinToString(", ")}")
                            null
                        }
                    } else null
                }
            }
            .toMap()
    }

    private suspend fun processStockDisclosure(
        code: String,
        dartCorpMap: Map<String, DartCorpCode>,
        fromDate: String,
        existingItemsMap: Map<String, List<DartListItem>>,
        failedStocks: MutableList<String>
    ): Int {
        val dartInfo = dartCorpMap[code] ?: return 0
        return try {
            var newCount = 0
            dartRemote.list(dartInfo.corpCode, fromDate)
                .collect { res ->
                    if (res.list?.isNotEmpty() == true) {
                        logD("${dartInfo.nameKr} - ${res.list.first().let {"${it.receiptDate} ${it.reportName}"} }")
                        items[code] = res.list.map {
                            it.copy(reportName = it.reportName.replace(Regex("\\s+"), " "))
                        }

                        // 새로운 공시 확인
                        val newDisclosures = findNewDisclosures(
                            items[code] ?: emptyList(),
                            existingItemsMap[code] ?: emptyList()
                        )

                        // 새로운 공시가 있으면 로그 출력
                        if (newDisclosures.isNotEmpty()) {
                            newCount = newDisclosures.size
                            newDisclosures.forEach { disclosure ->
                                logD("📢 새로운 공시: ${dartInfo.nameKr} - ${disclosure.reportName}")
                            }
                        }

                        updateSignal.emit(Unit)
                    }
                }
            newCount
        } catch (e: Exception) {
            logE("Error loading dart list for ${dartInfo.nameKr}: ${e.message}")
            synchronized(failedStocks) {
                failedStocks.add(dartInfo.nameKr)
            }
            0
        }
    }

    private fun findNewDisclosures(
        current: List<DartListItem>,
        existing: List<DartListItem>
    ): List<DartListItem> {
        val existingReceiptNums = existing.map { it.receiptNum }.toSet()
        return current.filter { it.receiptNum !in existingReceiptNums }
    }

    fun forceLoad() {
        logD("forceLoad()")
        clear()
        MainScope().launch(Dispatchers.IO) {
            val list = spacManager.spacList.value
            syncListToFirebase(list.map { it.code })
        }
    }

    fun hasDisclosure(code: String): Boolean {
        return items.containsKey(code)
    }

    private fun clear() {
        items.clear()
        lastUpdatedAt = 0L
    }

    // 오전 9:00 이전까지는 이전 날포함 가장 최근 장이 열리는 날로 결정
    private fun latestWorkDay(): LocalDate {
        val now = LocalDateTime.now()
        var date = if (now.hour < 9) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        while (date.isHoliday()) {
            date = date.minusDays(1)
        }
        return date
    }

}

data class DartCorpCode(
    val corpCode: String,
    val nameKr: String,
    val code: String,
)
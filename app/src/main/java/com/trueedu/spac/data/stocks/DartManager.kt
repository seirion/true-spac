package com.trueedu.spac.data.stocks

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
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DartManager @Inject constructor(
    private val local: Local,
    private val dartRemote: DartRemote,
    private val spacManager: SpacManager,
    private val firebaseDartManager: FirebaseDartManager,
) {
    companion object {
        private const val CACHE_VALIDITY_MINUTES = 30L
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
                .toDateTimeCompactString()
                .dropLast(2) // ss 제거 하여 yyyyMMddHHmm 으로 변환
                .toLong()
            val hasApiKey = local.dartApiKey.isNotBlank()

            if (hasApiKey && now - lastUpdatedAtRemote > CACHE_VALIDITY_MINUTES) {
                // 다시 로딩
                while (spacManager.loading.value) {
                    //logD("waiting spacManager")
                    delay(200)
                }
                val list = spacManager.spacList.value
                loadList(list.map { it.code })
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

    fun getSize(): Int {
        return items.size
    }

    fun getListMap(): Map<String, List<DartListItem>> {
        return items
    }

    suspend fun CoroutineScope.loadList(codes: List<String>) {
        if (local.dartApiKey.isBlank()) return

        // 이전 날짜 데이터 제거
        items.clear()

        val dartCorpMap = readDartCorpCode()
        val fromDate = latestWorkDay()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))

        // 모든 API 호출을 동시에 실행하고 결과를 기다림
        codes.map { code ->
            async {
                val dartInfo = dartCorpMap[code] ?: return@async
                try {
                    dartRemote.list(dartInfo.corpCode, fromDate)
                        .collect { res ->
                            if (res.list?.isNotEmpty() == true) {
                                logD("${dartInfo.nameKr} - ${res.list.first().let {"${it.receiptDate} ${it.reportName}"} }")
                                items[code] = res.list.map {
                                    it.copy(reportName = it.reportName.replace(Regex("\\s+"), " "))
                                }
                                updateSignal.emit(Unit)
                            }
                        }
                } catch (e: Exception) {
                    logE("Error loading dart list for ${dartInfo.nameKr}: ${e.message}")
                }
            }
        }.awaitAll()
        lastUpdatedAt = LocalDateTime.now()
            .toDateTimeCompactString()
            .dropLast(2) // ss 제거 하여 yyyyMMddHHmm 으로 변환
            .toLong()

        // 완료 후 firebase 업데이트
        firebaseDartManager.writeDartList(
            items.values.map {
                DartListResponse(status = "", message = "", list = it)
            }
        )
    }

    fun forceLoad() {
        logD("forceLoad()")
        clear()
        MainScope().launch(Dispatchers.IO) {
            val list = spacManager.spacList.value
            loadList(list.map { it.code })
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

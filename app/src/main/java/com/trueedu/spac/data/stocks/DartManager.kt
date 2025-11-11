package com.trueedu.spac.data.stocks

import com.trueedu.spac.dart.model.DartListItem
import com.trueedu.spac.dart.model.DartListResponse
import com.trueedu.spac.dart.repository.remote.DartRemote
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.log.logE
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

/**
 * DART API용 SPAC 종목 매핑 정보
 * 형식: corpCode nameKr code
 * 출처: DART 오픈API 기업 고유번호
 * TODO: 추후 동적 로딩으로 변경 고려
 */
private val dartCorpMap = """
01677429 엔에이치스팩27호 440820
01689938 신영스팩9호 445970
01689336 비엔케이제1호스팩 445360
01724417 하나29호스팩 454640
01724338 하나28호스팩 454750
01744703 SK증권제10호스팩 457940
01712616 엔에이치스팩29호 451700
01724709 KB제25호스팩 455250
01791321 하나30호스팩 469880
01785551 KB제27호스팩 464680
01683387 미래에셋드림스팩1호 442900
01696275 하나26호스팩 446750
01785056 엔에이치스팩30호 466910
01775952 한국제13호스팩 464440
01760640 교보14호스팩 456490
01792825 대신밸런스제17호스팩 471050
01738483 대신밸런스제16호스팩 457630
01725160 한화플러스제4호스팩 455310
01700374 하나27호스팩 448370
01797440 SK증권제11호스팩 472230
01787072 유진스팩10호 468760
01791561 하나31호스팩 469900
01800285 비엔케이제2호스팩 473370
01717170 상상인제4호스팩 452670
01705625 하이제8호스팩 450050
01700921 삼성스팩8호 448740
01725577 SK증권제9호스팩 455910
01792791 IBKS제24호스팩 469480
01692321 미래에셋비전스팩2호 446190
01781847 교보15호스팩 465320
01816268 KB제29호스팩 478390
01818682 한국제15호스팩 479880
01785700 IBKS제23호스팩 467930
01807729 하나33호스팩 475250
01815764 이베스트스팩6호 478110
01845701 교보16호스팩 482520
01798980 유안타제15호스팩 473050
01814233 미래에셋비전스팩4호 477380
01814589 디비금융스팩12호 477760
01866528 교보17호스팩 489210
01853214 신한제14호스팩 487360
01854408 신한제15호스팩 487830
01847550 하나34호스팩 484130
01807738 하나32호스팩 475240
01866926 KB제31호스팩 492220
01857991 디비금융제13호스팩 489730
01761296 한국제12호스팩 458610
01682740 IBKS제21호스팩 442770
01701328 IBKS제22호스팩 448760
01804476 유안타제16호스팩 474490
01817610 대신밸런스제18호스팩 478780
01801822 SK증권제13호스팩 473950
01877126 키움제10호스팩 487720
01817249 미래에셋비전스팩5호 477470
01834194 엔에이치스팩31호 481890
01845534 대신밸런스제19호스팩 482690
01873272 키움제11호스팩 489480
01872219 유안타제17호스팩 493790
01854392 유진스팩11호 488060
01881800 한화플러스제5호스팩 498390
01796858 신영스팩10호 472220
01813377 에이치엠씨제7호스팩 477340
01819867 미래에셋비전스팩6호 478440
01841635 미래에셋비전스팩7호 482680
01851650 KB제30호스팩 486630
01798722 SK증권제12호스팩 473000
01688346 유안타제11호스팩 444920
01690235 유안타제12호스팩 446150
01701753 유안타제13호스팩 449020
01706819 유안타제14호스팩 450940
01719105 신한제11호스팩 452980
01807747 신한제13호스팩 474930
01809569 신한제12호스팩 474660
01693922 키움제8호스팩 446840
01902990 KB제32호스팩 0037T0
01875146 신한제16호스팩 496070
01702424 미래에셋비전스팩3호 448830
01767494 에이치엠씨제6호스팩 462020
01884065 디비금융제14호스팩 0004Y0
01906552 엘에스스팩1호 0041J0
01906598 하나35호스팩 0041L0
01910324 교보18호스팩 0041B0
01910759 삼성스팩10호 0044K0
01934935 KB제33호스팩 0072Z0
01935183 삼성스팩11호 0071M0
""".trimIndent()
    .split("\n").map { line ->
        val parts = line.split(" ")
        DartCorpCode(
            corpCode = parts[0],
            nameKr = parts[1],
            code = parts[2]
        )
    }
    .associateBy(DartCorpCode::code)

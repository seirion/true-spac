package com.trueedu.spac.api.model.dto.firebase

import com.google.firebase.database.Exclude

/**
 * 미국 주식 정보
 * 데이터 형식 참고: mastcode 구조체 (해외종목코드정보)
 */
class UsStockInfo(
    val code: String,
    val nameKr: String,
    val attributes: String,
) {
    // 탭으로 구분된 필드들을 캐싱 (lazy 초기화)
    private val fields: List<String> by lazy {
        attributes.split('\t')
    }

    companion object {
        private const val EXPECTED_FIELD_COUNT = 24

        fun from(str: String): UsStockInfo {
            // 탭으로 구분된 데이터 파싱
            val fields = str.split('\t')
            if (fields.size < EXPECTED_FIELD_COUNT) {
                val preview = str.take(100).replace("\t", "\\t")
                throw IllegalArgumentException(
                    "Invalid data format: expected $EXPECTED_FIELD_COUNT fields, got ${fields.size}. Data preview: $preview..."
                )
            }

            val code = fields[4].trim()  // symb (심볼)
            val nameKr = fields[6].trim()  // knam (한글명)
            val attributes = str  // 전체 데이터를 attributes로 저장

            return UsStockInfo(code, nameKr, attributes)
        }

        // 탭으로 구분된 형식의 필드 인덱스 (0부터 시작)
        val fieldSpecs = listOf(
            0,      // ncod (National code) - US
            1,      // exid (Exchange id) - 22
            2,      // excd (Exchange code) - NAS
            3,      // exnm (Exchange name) - 나스닥
            4,      // symb (Symbol) - AACB
            5,      // rsym (realtime symbol) - NASAACB
            6,      // knam (Korea name) - 아티우스 애퀴지션 2
            7,      // enam (English name) - ARTIUS II ACQUISITION INC
            8,      // stis (Security type) - 2
            9,      // curr (currency) - USD
            10,     // zdiv (float position) - 4
            11,     // ztyp (data type) - (빈 필드)
            12,     // base (base price) - 10.2700
            13,     // bnit (Bid order size) - 1
            14,     // anit (Ask order size) - 1
            15,     // mstm (market start time) - 930
            16,     // metm (market end time) - 1600
            17,     // isdr (DR 여부) - N
            18,     // drcd (DR 국가코드) - (빈 필드)
            19,     // icod (업종분류코드) - 000
            20,     // sjong (지수구성종목 존재 여부) - 0
            21,     // ttyp (Tick size Type) - 0
            22,     // etyp (ETF/ETN/ETC type) - (공백)
            23      // ttyp_sb (Tick size type 상세) - (공백)
        )

        private val columns = mapOf(
            "국가코드" to 0,
            "거래소ID" to 1,
            "거래소코드" to 2,
            "거래소명" to 3,
            "심볼" to 4,
            "실시간심볼" to 5,
            "한글명" to 6,
            "영문명" to 7,
            "증권타입" to 8,
            "통화" to 9,
            "소수점자리" to 10,
            "데이터타입" to 11,
            "기준가" to 12,
            "매수호가단위" to 13,
            "매도호가단위" to 14,
            "시장시작시간" to 15,
            "시장종료시간" to 16,
            "DR여부" to 17,
            "DR국가코드" to 18,
            "업종분류코드" to 19,
            "지수구성종목여부" to 20,
            "호가단위타입" to 21,
            "ETP타입" to 22,
            "호가단위타입상세" to 23
        )
    }

    // No-argument constructor required for Firebase
    constructor() : this("", "", "")

    /** 국가코드 */
    @get:Exclude
    val nationalCode: String?
        get() = getAttribute("국가코드")?.trim()

    /** 거래소ID */
    @get:Exclude
    val exchangeId: String?
        get() = getAttribute("거래소ID")?.trim()

    /** 거래소코드 */
    @get:Exclude
    val exchangeCode: String?
        get() = getAttribute("거래소코드")?.trim()

    /** 거래소명 */
    @get:Exclude
    val exchangeName: String?
        get() = getAttribute("거래소명")?.trim()

    /** 실시간심볼 */
    @get:Exclude
    val realtimeSymbol: String?
        get() = getAttribute("실시간심볼")?.trim()

    /** 영문명 */
    @get:Exclude
    val englishName: String?
        get() = getAttribute("영문명")?.trim()

    /** 증권타입 (1:Index, 2:Stock, 3:ETP(ETF), 4:Warrant) */
    @get:Exclude
    val securityType: String?
        get() = getAttribute("증권타입")?.trim()

    /** 주식 여부 (증권타입 2) */
    @get:Exclude
    val isStock: Boolean
        get() = securityType == "2"

    /** 지수 여부 (증권타입 1) */
    @get:Exclude
    val isIndex: Boolean
        get() = securityType == "1"

    /** ETF 여부 (증권타입 3) */
    @get:Exclude
    val isEtf: Boolean
        get() = securityType == "3"

    /** 통화 */
    @get:Exclude
    val currency: String?
        get() = getAttribute("통화")?.trim()

    /** 소수점자리 */
    @get:Exclude
    val floatPosition: String?
        get() = getAttribute("소수점자리")?.trim()

    /** 기준가 */
    @get:Exclude
    val basePrice: String?
        get() = getAttribute("기준가")?.trim()

    /** 매수호가단위 */
    @get:Exclude
    val bidOrderUnit: String?
        get() = getAttribute("매수호가단위")?.trim()

    /** 매도호가단위 */
    @get:Exclude
    val askOrderUnit: String?
        get() = getAttribute("매도호가단위")?.trim()

    /** 시장시작시간 (HHMM) */
    @get:Exclude
    val marketStartTime: String?
        get() = getAttribute("시장시작시간")?.trim()

    /** 시장종료시간 (HHMM) */
    @get:Exclude
    val marketEndTime: String?
        get() = getAttribute("시장종료시간")?.trim()

    /** DR 여부 */
    @get:Exclude
    val isDr: Boolean
        get() = getAttribute("DR여부")?.trim() == "Y"

    /** DR 국가코드 */
    @get:Exclude
    val drCountryCode: String?
        get() = getAttribute("DR국가코드")?.trim()

    /** 업종분류코드 */
    @get:Exclude
    val industryCode: String?
        get() = getAttribute("업종분류코드")?.trim()

    /** 지수구성종목 존재 여부 (0:구성종목없음, 1:구성종목있음) */
    @get:Exclude
    val hasIndexConstituents: Boolean
        get() = getAttribute("지수구성종목여부")?.trim() == "1"

    /** 호가단위타입 */
    @get:Exclude
    val tickSizeType: String?
        get() = getAttribute("호가단위타입")?.trim()

    /** ETP 타입 (001:ETF, 002:ETN, 003:ETC, 004:Others, 005:VIX Underlying ETF, 006:VIX Underlying ETN) */
    @get:Exclude
    val etpType: String?
        get() = getAttribute("ETP타입")?.trim()

    /** 호가단위타입 상세 */
    @get:Exclude
    val tickSizeTypeDetail: String?
        get() = getAttribute("호가단위타입상세")?.trim()

    private fun getAttribute(key: String): String? {
        return columns[key]?.let { index ->
            fields.getOrNull(index)
        }
    }
}

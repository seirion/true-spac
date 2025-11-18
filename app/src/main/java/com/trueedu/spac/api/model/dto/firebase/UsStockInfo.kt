package com.trueedu.spac.api.model.dto.firebase

/**
 * 미국 주식 정보
 * 데이터 형식 참고: mastcode 구조체 (해외종목코드정보)
 */
class UsStockInfo(
    val code: String,
    val nameKr: String,
    val attributes: String,
) {

    companion object {
        fun from(str: String): UsStockInfo {
            if (str.length < ATTRIBUTES_LEN) {
                throw IllegalArgumentException("Invalid data length: ${str.length}, expected at least: $ATTRIBUTES_LEN")
            }

            val code = str.substring(22, 38).trim()  // symb
            val nameKr = str.substring(54, 118).trim()  // knam
            val attributes = str.substring(0, minOf(str.length, ATTRIBUTES_LEN))

            return UsStockInfo(code, nameKr, attributes)
        }

        private const val ATTRIBUTES_LEN = 242

        val fieldSpecs = listOf(
            0,      // ncod (National code)
            2,      // exid (Exchange id)
            5,      // excd (Exchange code)
            8,      // exnm (Exchange name)
            24,     // symb (Symbol)
            40,     // rsym (realtime symbol)
            56,     // knam (Korea name)
            120,    // enam (English name)
            184,    // stis (Security type)
            185,    // curr (currency)
            189,    // zdiv (float position)
            190,    // ztyp (data type)
            191,    // base (base price)
            203,    // bnit (Bid order size)
            211,    // anit (Ask order size)
            219,    // mstm (market start time)
            223,    // metm (market end time)
            227,    // isdr (DR 여부)
            228,    // drcd (DR 국가코드)
            230,    // icod (업종분류코드)
            234,    // sjong (지수구성종목 존재 여부)
            235,    // ttyp (Tick size Type)
            236,    // etyp (ETF/ETN/ETC type)
            239,    // ttyp_sb (Tick size type 상세)
            242     // 끝
        )

        private val columns = listOf(
            "국가코드",
            "거래소ID",
            "거래소코드",
            "거래소명",
            "심볼",
            "실시간심볼",
            "한글명",
            "영문명",
            "증권타입",
            "통화",
            "소수점자리",
            "데이터타입",
            "기준가",
            "매수호가단위",
            "매도호가단위",
            "시장시작시간",
            "시장종료시간",
            "DR여부",
            "DR국가코드",
            "업종분류코드",
            "지수구성종목여부",
            "호가단위타입",
            "ETP타입",
            "호가단위타입상세"
        )
            .mapIndexed { index, s -> s to index }
            .toMap()
    }

    // No-argument constructor required for Firebase
    constructor() : this("", "", "")

    /** 국가코드 */
    val nationalCode: String?
        get() = getAttribute("국가코드")?.trim()

    /** 거래소ID */
    val exchangeId: String?
        get() = getAttribute("거래소ID")?.trim()

    /** 거래소코드 */
    val exchangeCode: String?
        get() = getAttribute("거래소코드")?.trim()

    /** 거래소명 */
    val exchangeName: String?
        get() = getAttribute("거래소명")?.trim()

    /** 실시간심볼 */
    val realtimeSymbol: String?
        get() = getAttribute("실시간심볼")?.trim()

    /** 영문명 */
    val englishName: String?
        get() = getAttribute("영문명")?.trim()

    /** 증권타입 (1:Index, 2:Stock, 3:ETP(ETF), 4:Warrant) */
    val securityType: String?
        get() = getAttribute("증권타입")?.trim()

    /** 주식 여부 (증권타입 2) */
    val isStock: Boolean
        get() = securityType == "2"

    /** 지수 여부 (증권타입 1) */
    val isIndex: Boolean
        get() = securityType == "1"

    /** ETF 여부 (증권타입 3) */
    val isEtf: Boolean
        get() = securityType == "3"

    /** 통화 */
    val currency: String?
        get() = getAttribute("통화")?.trim()

    /** 소수점자리 */
    val floatPosition: String?
        get() = getAttribute("소수점자리")?.trim()

    /** 기준가 */
    val basePrice: String?
        get() = getAttribute("기준가")?.trim()

    /** 매수호가단위 */
    val bidOrderUnit: String?
        get() = getAttribute("매수호가단위")?.trim()

    /** 매도호가단위 */
    val askOrderUnit: String?
        get() = getAttribute("매도호가단위")?.trim()

    /** 시장시작시간 (HHMM) */
    val marketStartTime: String?
        get() = getAttribute("시장시작시간")?.trim()

    /** 시장종료시간 (HHMM) */
    val marketEndTime: String?
        get() = getAttribute("시장종료시간")?.trim()

    /** DR 여부 */
    val isDr: Boolean
        get() = getAttribute("DR여부")?.trim() == "Y"

    /** DR 국가코드 */
    val drCountryCode: String?
        get() = getAttribute("DR국가코드")?.trim()

    /** 업종분류코드 */
    val industryCode: String?
        get() = getAttribute("업종분류코드")?.trim()

    /** 지수구성종목 존재 여부 (0:구성종목없음, 1:구성종목있음) */
    val hasIndexConstituents: Boolean
        get() = getAttribute("지수구성종목여부")?.trim() == "1"

    /** 호가단위타입 */
    val tickSizeType: String?
        get() = getAttribute("호가단위타입")?.trim()

    /** ETP 타입 (001:ETF, 002:ETN, 003:ETC, 004:Others, 005:VIX Underlying ETF, 006:VIX Underlying ETN) */
    val etpType: String?
        get() = getAttribute("ETP타입")?.trim()

    /** 호가단위타입 상세 */
    val tickSizeTypeDetail: String?
        get() = getAttribute("호가단위타입상세")?.trim()

    private fun getAttribute(key: String): String? {
        return columns[key]?.let { index ->
            if (index >= 0 && index < fieldSpecs.size - 1) {
                val start = fieldSpecs[index]
                val end = fieldSpecs[index + 1]
                if (attributes.length >= end) {
                    return attributes.substring(start, end)
                }
            }
            null
        }
    }
}

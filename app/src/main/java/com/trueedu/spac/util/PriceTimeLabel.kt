package com.trueedu.spac.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 홈 화면에 표시하는 시세 기준 시각 문구.
 *
 * | 시세 저장 시각(오늘)        | 문구        |
 * |---------------------------|------------|
 * | 시세 정보 없음/오늘 이전      | 전일 종가    |
 * | 15:30 까지                  | HH:mm      |
 * | 15:30 초과 ~ 16:00 미만      | 15:30      |  (정규장 종가)
 * | 16:00 ~ 20:00 미만 (애프터마켓) | A19:00     |  (A + 실제 시각)
 * | 20:00 이후                  | 20:00      |  (애프터마켓 종가)
 *
 * @param timestamp yyyyMMddHHmm 형식의 시세 기준 시각 (없으면 0)
 */
fun priceTimeLabel(timestamp: Long, today: LocalDate = LocalDate.now()): String {
    if (timestamp == 0L) return PRICE_TIME_PREV_CLOSE

    val timestampStr = timestamp.toString()
    if (timestampStr.length != 12) return PRICE_TIME_PREV_CLOSE

    val cacheDate = runCatching {
        LocalDate.parse(timestampStr.take(8), DateTimeFormatter.ofPattern("yyyyMMdd"))
    }.getOrNull() ?: return PRICE_TIME_PREV_CLOSE
    if (cacheDate.isBefore(today)) return PRICE_TIME_PREV_CLOSE

    val time = runCatching {
        LocalTime.parse(timestampStr.substring(8, 12), DateTimeFormatter.ofPattern("HHmm"))
    }.getOrNull() ?: return PRICE_TIME_PREV_CLOSE

    return when {
        !time.isAfter(REGULAR_CLOSE) -> DateTimeFormatter.ofPattern("HH:mm").format(time)
        time < AFTER_MARKET_OPEN -> "15:30"
        time < AFTER_MARKET_CLOSE -> "A" + DateTimeFormatter.ofPattern("HH:mm").format(time)
        else -> "20:00"
    }
}

const val PRICE_TIME_PREV_CLOSE = "전일 종가"
private val REGULAR_CLOSE = LocalTime.of(15, 30)
private val AFTER_MARKET_OPEN = LocalTime.of(16, 0)
private val AFTER_MARKET_CLOSE = LocalTime.of(20, 0)

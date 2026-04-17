package com.trueedu.spac.data.stocks

import com.trueedu.spac.data.log.logE
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 마스터 파일이 업로드 되는 시각 (HHmm)
 */
private val uploadTime = listOf(
    600,
    // 655,
    // 735,
    // 755,
    // 845,
    // 946,
    // 1055,
    // 1710,
    // 1730,
    // 1755,
    // 1810,
    1830,
    // 1855,
)

/**
 * 리모트에 더 최신 종목 정보가 있는 지 여부
 *
 */
fun needUpdateRemoteData(localTimestamp: Long, remoteTimestamp: Long): Boolean {

    if (localTimestamp == 0L) return true

    val localDate = localTimestamp / 10000L
    val remoteDate = remoteTimestamp / 10000L

    // 로컬 데이터가 더 이전 날짜 것이면
    if (localDate < remoteDate) {
        // 주말이면 데이터 업데이트 불필요
        val localDayOfWeek = dayOfWeek(localDate)
        val remoteDayOfWeek = dayOfWeek(remoteDate)

        // 토요일 → 일요일 케이스
        if (localDate + 1 == remoteDate &&
            localDayOfWeek == DayOfWeek.SATURDAY && remoteDayOfWeek == DayOfWeek.SUNDAY) {
            return false
        }

        // 금요일 → 토요일 케이스
        if (localDate + 1 == remoteDate &&
            localDayOfWeek == DayOfWeek.FRIDAY && remoteDayOfWeek == DayOfWeek.SATURDAY) {
            return false
        }

        // 금요일 → 일요일 케이스
        if (localDate + 2 == remoteDate &&
            localDayOfWeek == DayOfWeek.FRIDAY && remoteDayOfWeek == DayOfWeek.SUNDAY) {
            return false
        }

        return true
    } else if (localDate == remoteDate) { // 날짜가 같을 때

        val localHHmm = localTimestamp % 10000L
        val remoteHHmm = remoteTimestamp % 10000L

        // Check if remote time falls within the update intervals
        return uploadTime.any { it in (localHHmm + 1)..remoteHHmm }
    } else {
        return false
    }
}

private fun dayOfWeek(yyyyMMdd: Long): DayOfWeek {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val localDate = LocalDate.parse(yyyyMMdd.toString(), formatter)
    return localDate.dayOfWeek
}

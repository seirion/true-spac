package com.trueedu.spac.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

fun LocalDate.isHoliday(): Boolean {
    return when (this.dayOfWeek) {
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY -> true
        else -> {
            holidays.contains(this)
        }
    }
}

// 몇 개 안 되니까 그냥 하드 코딩
// 주식 장이 열리지 않는 날
private val holidays = setOf(
    LocalDate.of(2026, 3, 2),
    LocalDate.of(2026, 5, 1),
    LocalDate.of(2026, 5, 5),
    LocalDate.of(2026, 5, 25),
    LocalDate.of(2026, 6, 3), // 지방선거
    LocalDate.of(2026, 7, 17),
    LocalDate.of(2026, 8, 17),
    LocalDate.of(2026, 9, 24),
    LocalDate.of(2026, 9, 25),
    LocalDate.of(2026, 10, 5), // 대체휴일
    LocalDate.of(2026, 10, 9),
    LocalDate.of(2026, 12, 25),
    LocalDate.of(2026, 12, 31),
    LocalDate.of(2027, 1, 1),   // 신정
    LocalDate.of(2027, 2, 8),   // 설날 연휴
    LocalDate.of(2027, 2, 9),   // 대체공휴일(설날)
    LocalDate.of(2027, 3, 1),   // 3·1절
    LocalDate.of(2027, 5, 5),   // 어린이날
    LocalDate.of(2027, 5, 13),  // 부처님 오신날
    LocalDate.of(2027, 8, 16),  // 대체공휴일(광복절)
    LocalDate.of(2027, 9, 14),  // 추석 연휴
    LocalDate.of(2027, 9, 15),  // 추석
    LocalDate.of(2027, 9, 16),  // 추석 연휴
    LocalDate.of(2027, 10, 4),  // 대체공휴일(개천절)
    LocalDate.of(2027, 10, 11), // 대체공휴일(한글날)
    LocalDate.of(2027, 12, 27), // 대체공휴일(크리스마스)
)

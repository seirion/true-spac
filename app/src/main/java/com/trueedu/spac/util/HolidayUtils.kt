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
    LocalDate.of(2025, 12, 31),
    LocalDate.of(2026, 1, 1),
    LocalDate.of(2026, 2, 16),
    LocalDate.of(2026, 2, 17),
    LocalDate.of(2026, 2, 18),
    LocalDate.of(2026, 3, 2),
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
)

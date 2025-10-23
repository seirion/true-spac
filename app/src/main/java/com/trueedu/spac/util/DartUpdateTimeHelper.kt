package com.trueedu.spac.util

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * DART 공시 업데이트 시간 관련 유틸리티
 * 평일 9:00 ~ 23:00 사이에만 동작
 */
object DartUpdateTimeHelper {

    // DART 업데이트 시간
    private val UPDATE_START_TIME = LocalTime.of(9, 0)  // 09:00
    private val UPDATE_END_TIME = LocalTime.of(23, 0)  // 23:00

    /**
     * 현재 시간이 업데이트 시간인지 확인
     * - 평일 09:00 - 23:00
     * - 공휴일 제외
     */
    fun isUpdateTime(dateTime: LocalDateTime = LocalDateTime.now()): Boolean {
        val date = dateTime.toLocalDate()
        val time = dateTime.toLocalTime()

        // 주말 또는 공휴일 체크
        if (date.isHoliday()) {
            return false
        }

        // 업데이트 시간 체크
        return time >= UPDATE_START_TIME && time < UPDATE_END_TIME
    }

    /**
     * 다음 업데이트 시작 시간을 반환
     */
    fun getNextUpdateStartTime(dateTime: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        var nextDate = dateTime.toLocalDate()
        var nextTime = UPDATE_START_TIME

        // 현재 시간이 업데이트 종료 이전이면 오늘
        if (dateTime.toLocalTime() < UPDATE_END_TIME && !nextDate.isHoliday()) {
            return LocalDateTime.of(nextDate, nextTime)
        }

        // 다음 업데이트 날짜 찾기
        do {
            nextDate = nextDate.plusDays(1)
        } while (nextDate.isHoliday())

        return LocalDateTime.of(nextDate, nextTime)
    }

    /**
     * 다음 업데이트 종료 시간을 반환
     */
    fun getNextUpdateEndTime(dateTime: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        var nextDate = dateTime.toLocalDate()

        // 오늘이 업데이트 날짜이고 종료 전이면 오늘
        if (!nextDate.isHoliday() && dateTime.toLocalTime() < UPDATE_END_TIME) {
            return LocalDateTime.of(nextDate, UPDATE_END_TIME)
        }

        // 다음 업데이트 날짜 찾기
        do {
            nextDate = nextDate.plusDays(1)
        } while (nextDate.isHoliday())

        return LocalDateTime.of(nextDate, UPDATE_END_TIME)
    }

    /**
     * 업데이트 시작까지 남은 시간(밀리초)
     */
    fun getMillisUntilUpdateStart(dateTime: LocalDateTime = LocalDateTime.now()): Long {
        val nextStart = getNextUpdateStartTime(dateTime)
        return java.time.Duration.between(dateTime, nextStart).toMillis()
    }

    /**
     * 업데이트 종료까지 남은 시간(밀리초)
     */
    fun getMillisUntilUpdateEnd(dateTime: LocalDateTime = LocalDateTime.now()): Long {
        val nextEnd = getNextUpdateEndTime(dateTime)
        return java.time.Duration.between(dateTime, nextEnd).toMillis()
    }
}

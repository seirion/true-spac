package com.trueedu.spac.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 주식 거래 시간 관련 유틸리티
 */
object TradingTimeHelper {

    // 한국 주식 시장 거래 시간
    private val MARKET_OPEN_TIME = LocalTime.of(9, 0)  // 09:00
    private val MARKET_CLOSE_TIME = LocalTime.of(15, 30)  // 15:30
    // 장 종료 직후 종가 반영 지연을 고려한 추가 업데이트 시간
    // 15:30 직후에는 종가가 즉시 반영되지 않는 경우가 있어 15:40까지 업데이트를 허용한다.
    private val PRICE_UPDATE_END_TIME = LocalTime.of(15, 40)  // 15:40

    /**
     * 현재 시간이 거래 시간인지 확인
     * - 평일 09:00 - 15:30
     * - 공휴일 제외
     */
    fun isTradingTime(dateTime: LocalDateTime = LocalDateTime.now()): Boolean {
        val date = dateTime.toLocalDate()
        val time = dateTime.toLocalTime()

        // 주말 또는 공휴일 체크
        if (date.isHoliday()) {
            return false
        }

        // 거래 시간 체크
        return time >= MARKET_OPEN_TIME && time < MARKET_CLOSE_TIME
    }

    /**
     * 현재 시간이 가격 업데이트 허용 시간인지 확인
     * - 평일 09:00 - 15:40
     * - 공휴일 제외
     *
     * 장 종료(15:30) 직후 종가가 늦게 반영되는 케이스를 위해 여유 구간을 둔다.
     */
    fun isPriceUpdateTime(dateTime: LocalDateTime = LocalDateTime.now()): Boolean {
        val date = dateTime.toLocalDate()
        val time = dateTime.toLocalTime()

        if (date.isHoliday()) {
            return false
        }

        return time >= MARKET_OPEN_TIME && time < PRICE_UPDATE_END_TIME
    }

    /**
     * 다음 거래 시작 시간을 반환
     */
    fun getNextTradingStartTime(dateTime: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        var nextDate = dateTime.toLocalDate()
        var nextTime = MARKET_OPEN_TIME

        // 현재 시간이 장 마감 이전이면 오늘
        if (dateTime.toLocalTime() < MARKET_CLOSE_TIME && !nextDate.isHoliday()) {
            return LocalDateTime.of(nextDate, nextTime)
        }

        // 다음 거래일 찾기
        do {
            nextDate = nextDate.plusDays(1)
        } while (nextDate.isHoliday())

        return LocalDateTime.of(nextDate, nextTime)
    }

    /**
     * 다음 거래 종료 시간을 반환
     */
    fun getNextTradingEndTime(dateTime: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        var nextDate = dateTime.toLocalDate()

        // 오늘이 거래일이고 장 마감 전이면 오늘
        if (!nextDate.isHoliday() && dateTime.toLocalTime() < MARKET_CLOSE_TIME) {
            return LocalDateTime.of(nextDate, MARKET_CLOSE_TIME)
        }

        // 다음 거래일 찾기
        do {
            nextDate = nextDate.plusDays(1)
        } while (nextDate.isHoliday())

        return LocalDateTime.of(nextDate, MARKET_CLOSE_TIME)
    }

    /**
     * 다음 가격 업데이트 종료 시간을 반환 (15:40)
     */
    fun getNextPriceUpdateEndTime(dateTime: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        var nextDate = dateTime.toLocalDate()

        // 오늘이 거래일이고 업데이트 종료 전이면 오늘
        if (!nextDate.isHoliday() && dateTime.toLocalTime() < PRICE_UPDATE_END_TIME) {
            return LocalDateTime.of(nextDate, PRICE_UPDATE_END_TIME)
        }

        // 다음 거래일 찾기
        do {
            nextDate = nextDate.plusDays(1)
        } while (nextDate.isHoliday())

        return LocalDateTime.of(nextDate, PRICE_UPDATE_END_TIME)
    }

    /**
     * 거래 시작까지 남은 시간(밀리초)
     */
    fun getMillisUntilTradingStart(dateTime: LocalDateTime = LocalDateTime.now()): Long {
        val now = dateTime
        val nextStart = getNextTradingStartTime(dateTime)
        return java.time.Duration.between(now, nextStart).toMillis()
    }

    /**
     * 거래 종료까지 남은 시간(밀리초)
     */
    fun getMillisUntilTradingEnd(dateTime: LocalDateTime = LocalDateTime.now()): Long {
        val now = dateTime
        val nextEnd = getNextTradingEndTime(dateTime)
        return java.time.Duration.between(now, nextEnd).toMillis()
    }

    /**
     * 가격 업데이트 종료까지 남은 시간(밀리초)
     */
    fun getMillisUntilPriceUpdateEnd(dateTime: LocalDateTime = LocalDateTime.now()): Long {
        val now = dateTime
        val nextEnd = getNextPriceUpdateEndTime(dateTime)
        return java.time.Duration.between(now, nextEnd).toMillis()
    }

    /**
     * 오늘 날짜의 장 종료 시각(15:30) 반환
     */
    fun getTodayMarketCloseTime(): LocalDateTime {
        return LocalDateTime.of(LocalDate.now(), MARKET_CLOSE_TIME)
    }

    /**
     * 오늘 날짜의 가격 업데이트 종료 시각(15:40) 반환
     */
    fun getTodayPriceUpdateEndTime(): LocalDateTime {
        return LocalDateTime.of(LocalDate.now(), PRICE_UPDATE_END_TIME)
    }
}


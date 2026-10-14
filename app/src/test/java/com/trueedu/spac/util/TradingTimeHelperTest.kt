package com.trueedu.spac.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TradingTimeHelperTest {
    // 2026-10-14 수요일 (평일, 휴장일 아님)
    private val weekday = LocalDate.of(2026, 10, 14)
    private val saturday = LocalDate.of(2026, 10, 17)

    private fun at(date: LocalDate, h: Int, m: Int) = LocalDateTime.of(date, java.time.LocalTime.of(h, m))

    @Test
    fun `테스트 기준일은 평일이고 휴장일이 아니다`() {
        assertFalse(weekday.isHoliday())
        assertTrue(saturday.isHoliday())
    }

    @Test
    fun `업데이트 시간은 평일 09시부터 20시 10분 전까지`() {
        assertFalse(TradingTimeHelper.isPriceUpdateTime(at(weekday, 8, 59)))
        assertTrue(TradingTimeHelper.isPriceUpdateTime(at(weekday, 9, 0)))
        assertTrue(TradingTimeHelper.isPriceUpdateTime(at(weekday, 15, 40)))   // 이전 종료 시각 이후에도 허용
        assertTrue(TradingTimeHelper.isPriceUpdateTime(at(weekday, 18, 30)))   // 애프터마켓
        assertTrue(TradingTimeHelper.isPriceUpdateTime(at(weekday, 20, 9)))
        assertFalse(TradingTimeHelper.isPriceUpdateTime(at(weekday, 20, 10)))
        assertFalse(TradingTimeHelper.isPriceUpdateTime(at(weekday, 23, 0)))
    }

    @Test
    fun `주말과 휴장일에는 업데이트하지 않는다`() {
        assertFalse(TradingTimeHelper.isPriceUpdateTime(at(saturday, 12, 0)))
        assertFalse(TradingTimeHelper.isPriceUpdateTime(at(saturday, 18, 0)))
    }

    @Test
    fun `정규 거래 시간은 여전히 15시 30분까지`() {
        assertTrue(TradingTimeHelper.isTradingTime(at(weekday, 15, 29)))
        assertFalse(TradingTimeHelper.isTradingTime(at(weekday, 15, 30)))
        assertFalse(TradingTimeHelper.isTradingTime(at(weekday, 18, 0)))
    }

    @Test
    fun `업데이트 종료 시각은 평일 20시 10분이고 지나면 다음 거래일`() {
        assertEquals(at(weekday, 20, 10), TradingTimeHelper.getNextPriceUpdateEndTime(at(weekday, 10, 0)))
        assertEquals(at(weekday, 20, 10), TradingTimeHelper.getNextPriceUpdateEndTime(at(weekday, 18, 0)))
        // 20:10 이후에는 다음 거래일 (수요일 → 목요일, 휴장이 아니면)
        val next = TradingTimeHelper.getNextPriceUpdateEndTime(at(weekday, 20, 30))
        assertEquals(LocalDate.of(2026, 10, 15), next.toLocalDate())
        assertEquals(java.time.LocalTime.of(20, 10), next.toLocalTime())
        // 토요일에는 다음 평일(월요일) 20:10
        val fromSat = TradingTimeHelper.getNextPriceUpdateEndTime(at(saturday, 12, 0))
        assertEquals(LocalDate.of(2026, 10, 19), fromSat.toLocalDate())
    }
}

package com.trueedu.spac.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PriceTimeLabelTest {
    private val today = LocalDate.of(2026, 10, 14)
    private fun label(hhmm: String, date: String = "20261014") = priceTimeLabel("$date$hhmm".toLong(), today)

    @Test
    fun `시세 정보가 없거나 오늘 이전이면 전일 종가`() {
        assertEquals("전일 종가", priceTimeLabel(0L, today))
        assertEquals("전일 종가", priceTimeLabel(12345L, today))
        assertEquals("전일 종가", label("1000", date = "20261013"))
        assertEquals("전일 종가", priceTimeLabel(202613991000L, today)) // 잘못된 날짜
    }

    @Test
    fun `정규장 시간에는 실제 시각`() {
        assertEquals("09:00", label("0900"))
        assertEquals("13:40", label("1340"))
        assertEquals("15:30", label("1530"))
    }

    @Test
    fun `정규장 종료 후 애프터마켓 시작 전에는 15시 30분`() {
        assertEquals("15:30", label("1531"))
        assertEquals("15:30", label("1540"))
        assertEquals("15:30", label("1559"))
    }

    @Test
    fun `애프터마켓 시간에는 A 와 실제 시각`() {
        assertEquals("A16:00", label("1600"))
        assertEquals("A18:40", label("1840"))
        assertEquals("A19:00", label("1900"))
        assertEquals("A19:59", label("1959"))
    }

    @Test
    fun `20시 이후에는 20시 00분`() {
        assertEquals("20:00", label("2000"))
        assertEquals("20:00", label("2010"))
    }
}

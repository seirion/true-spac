package com.trueedu.spac.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import kotlin.math.abs

class ProfitCalculatorTest {

    @Test
    fun `청산 수익률 계산 - 정상 케이스`() {
        val currentPrice = 10000.0
        val redemptionPrice = 11000.0
        val targetDate = LocalDate.now().plusDays(180)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        // 청산 수익률은 10%
        assertEquals(10.0, result.first!!, 0.01)
        // 연환산 수익률 검증 (복리)
        assertNotNull(result.second)
    }

    @Test
    fun `청산 수익률 계산 - 365일 후`() {
        val currentPrice = 10000.0
        val redemptionPrice = 11000.0
        val targetDate = LocalDate.now().plusDays(365)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        // 청산 수익률은 10%
        assertEquals(10.0, result.first!!, 0.01)
        // 1년 후이므로 연환산 수익률도 10%
        assertEquals(10.0, result.second!!, 0.01)
    }

    @Test
    fun `청산 수익률 계산 - 단리 vs 복리 비교`() {
        val currentPrice = 10000.0
        val redemptionPrice = 11000.0
        val targetDate = LocalDate.now().plusDays(180)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        // 단리 계산: 10% * 365 / 180 = 20.28%
        val simpleInterest = 10.0 * 365 / 180

        // 복리 계산이 단리보다 커야 함
        assert(result.second!! > simpleInterest)
    }

    @Test
    fun `청산 수익률 계산 - 높은 수익률 장기간`() {
        val currentPrice = 10000.0
        val redemptionPrice = 15000.0
        val targetDate = LocalDate.now().plusDays(730) // 2년

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        // 청산 수익률은 50%
        assertEquals(50.0, result.first!!, 0.01)
        // 연환산 수익률 검증 (복리): (1.5)^(1/2) - 1 ≈ 22.47%
        assertEquals(22.47, result.second!!, 0.1)
    }

    @Test
    fun `청산 수익률 계산 - 손실 케이스`() {
        val currentPrice = 10000.0
        val redemptionPrice = 9500.0
        val targetDate = LocalDate.now().plusDays(180)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        // 청산 수익률은 -5%
        assertEquals(-5.0, result.first!!, 0.01)
        // 연환산 수익률도 음수
        assert(result.second!! < 0)
    }

    @Test
    fun `청산 수익률 계산 - 짧은 기간 (7일)`() {
        val currentPrice = 10000.0
        val redemptionPrice = 10100.0
        val targetDate = LocalDate.now().plusDays(7)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        // 청산 수익률은 1%
        assertEquals(1.0, result.first!!, 0.01)
        // 연환산 수익률은 매우 높게 나옴 (복리 효과)
        assertNotNull(result.second)
    }

    @Test
    fun `청산 수익률 계산 - 1일 후 케이스`() {
        val currentPrice = 10000.0
        val redemptionPrice = 10100.0
        val targetDate = LocalDate.now().plusDays(1)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        // 청산 수익률은 1%
        assertEquals(1.0, result.first!!, 0.01)
        // 연환산 수익률 검증
        assertNotNull(result.second)
    }

    @Test
    fun `잘못된 입력 - 현재가가 0 이하`() {
        val currentPrice = 0.0
        val redemptionPrice = 11000.0
        val targetDate = LocalDate.now().plusDays(180)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `잘못된 입력 - 현재가가 음수`() {
        val currentPrice = -10000.0
        val redemptionPrice = 11000.0
        val targetDate = LocalDate.now().plusDays(180)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `잘못된 입력 - 청산가가 0 이하`() {
        val currentPrice = 10000.0
        val redemptionPrice = 0.0
        val targetDate = LocalDate.now().plusDays(180)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `잘못된 입력 - 청산일이 과거`() {
        val currentPrice = 10000.0
        val redemptionPrice = 11000.0
        val targetDate = LocalDate.now().minusDays(1)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `잘못된 입력 - 청산일이 오늘`() {
        val currentPrice = 10000.0
        val redemptionPrice = 11000.0
        val targetDate = LocalDate.now()

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        assertNull(result.first)
        assertNull(result.second)
    }

    @Test
    fun `복리 계산 정확도 검증 - 수학적 공식`() {
        val currentPrice = 10000.0
        val redemptionPrice = 12000.0
        val days = 365

        val targetDate = LocalDate.now().plusDays(days.toLong())
        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        // 수동 계산: (12000/10000)^(365/365) - 1 = 0.2 = 20%
        val expectedAnnualized = 20.0
        assertEquals(expectedAnnualized, result.second!!, 0.01)
    }

    @Test
    fun `소수점 이하 가격 처리`() {
        val currentPrice = 12345.67
        val redemptionPrice = 13456.78
        val targetDate = LocalDate.now().plusDays(200)

        val result = redemptionProfitRate(currentPrice, redemptionPrice, targetDate)

        // 수익률 계산 검증
        val expectedProfit = (13456.78 - 12345.67) / 12345.67 * 100
        assertEquals(expectedProfit, result.first!!, 0.01)
        assertNotNull(result.second)
    }

    private fun assertNotNull(value: Double?) {
        assert(value != null) { "Value should not be null" }
    }
}


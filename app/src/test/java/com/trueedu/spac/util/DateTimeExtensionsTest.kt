package com.trueedu.spac.util

import com.trueedu.spac.api.model.dto.auth.TokenResponse
import com.trueedu.spac.api.model.dto.order.OrderDetail
import com.trueedu.spac.api.model.dto.price.DailyPrice
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * DateTimeExtensions 유닛 테스트
 */
class DateTimeExtensionsTest {

    @Test
    fun `TokenResponse - getExpiredDateTime 변환 성공`() {
        // Given
        val tokenResponse = TokenResponse(
            accessToken = "test_token",
            tokenType = "Bearer",
            expiresIn = 7776000,
            accessTokenTokenExpired = "2025-10-12 01:30:45"
        )

        // When
        val expiredDateTime = tokenResponse.getExpiredDateTime()

        // Then
        assertNotNull(expiredDateTime)
        assertEquals(2025, expiredDateTime?.year)
        assertEquals(10, expiredDateTime?.monthValue)
        assertEquals(12, expiredDateTime?.dayOfMonth)
        assertEquals(1, expiredDateTime?.hour)
        assertEquals(30, expiredDateTime?.minute)
        assertEquals(45, expiredDateTime?.second)
    }

    @Test
    fun `TokenResponse - 잘못된 형식은 null 반환`() {
        // Given
        val tokenResponse = TokenResponse(
            accessToken = "test_token",
            tokenType = "Bearer",
            expiresIn = 7776000,
            accessTokenTokenExpired = "invalid_date"
        )

        // When
        val expiredDateTime = tokenResponse.getExpiredDateTime()

        // Then
        assertNull(expiredDateTime)
    }

    @Test
    fun `DailyPrice - getDate 변환 성공`() {
        // Given
        val dailyPrice = DailyPrice(
            date = "20251012",
            close = "50000",
            open = "49000",
            high = "51000",
            low = "48500",
            volume = "1000000",
            volumeAmount = "50000000000",
            changeSign = "2",
            change = "1000"
        )

        // When
        val date = dailyPrice.getDate()

        // Then
        assertNotNull(date)
        assertEquals(2025, date?.year)
        assertEquals(10, date?.monthValue)
        assertEquals(12, date?.dayOfMonth)
    }

    @Test
    fun `DailyPrice - null 날짜는 null 반환`() {
        // Given
        val dailyPrice = DailyPrice(
            date = null,
            close = "50000",
            open = "49000",
            high = "51000",
            low = "48500",
            volume = "1000000",
            volumeAmount = "50000000000",
            changeSign = "2",
            change = "1000"
        )

        // When
        val date = dailyPrice.getDate()

        // Then
        assertNull(date)
    }

    @Test
    fun `OrderDetail - getOrderTime 변환 성공`() {
        // Given
        val orderDetail = OrderDetail(
            krxForwardingOrderOrgNumber = "12345",
            orderNumber = "67890",
            orderTime = "013045"
        )

        // When
        val orderTime = orderDetail.getOrderTime()

        // Then
        assertNotNull(orderTime)
        assertEquals(1, orderTime?.hour)
        assertEquals(30, orderTime?.minute)
        assertEquals(45, orderTime?.second)
    }

    @Test
    fun `OrderDetail - 잘못된 시간 형식은 null 반환`() {
        // Given
        val orderDetail = OrderDetail(
            krxForwardingOrderOrgNumber = "12345",
            orderNumber = "67890",
            orderTime = "invalid"
        )

        // When
        val orderTime = orderDetail.getOrderTime()

        // Then
        assertNull(orderTime)
    }

    // ========================================
    // 실제 사용 시나리오 테스트
    // ========================================

    @Test
    fun `시나리오 - 일별 가격 데이터를 날짜로 필터링`() {
        // Given
        val prices = listOf(
            DailyPrice("20251010", "50000", "49000", "51000", "48500", "1000000", "50000000000", "2", "1000"),
            DailyPrice("20251011", "51000", "50000", "52000", "49500", "1100000", "55000000000", "2", "1000"),
            DailyPrice("20251012", "52000", "51000", "53000", "50500", "1200000", "60000000000", "2", "1000"),
            DailyPrice("20251013", "53000", "52000", "54000", "51500", "1300000", "65000000000", "2", "1000")
        )

        val targetDate = LocalDate.of(2025, 10, 12)

        // When
        val filteredPrices = prices.filter { it.getDate() == targetDate }

        // Then
        assertEquals(1, filteredPrices.size)
        assertEquals("52000", filteredPrices.first().close)
    }

    @Test
    fun `시나리오 - 최근 7일 데이터 필터링`() {
        // Given
        val today = LocalDate.of(2025, 10, 12)
        val sevenDaysAgo = today.minusDays(7) // 2025-10-05

        val prices = listOf(
            DailyPrice("20251001", "50000", "49000", "51000", "48500", "1000000", "50000000000", "2", "1000"), // 10-01 (제외)
            DailyPrice("20251008", "51000", "50000", "52000", "49500", "1100000", "55000000000", "2", "1000"), // 10-08 (포함)
            DailyPrice("20251010", "52000", "51000", "53000", "50500", "1200000", "60000000000", "2", "1000"), // 10-10 (포함)
            DailyPrice("20251012", "53000", "52000", "54000", "51500", "1300000", "65000000000", "2", "1000")  // 10-12 (포함)
        )

        // When
        val recentPrices = prices.filter { price ->
            price.getDate()?.let { date ->
                (date.isAfter(sevenDaysAgo) || date.isEqual(sevenDaysAgo)) && !date.isAfter(today)
            } ?: false
        }

        // Then
        assertEquals(3, recentPrices.size)
    }

    @Test
    fun `시나리오 - 주문 시간이 오전 9시 이후인 주문만 필터링`() {
        // Given
        val orders = listOf(
            OrderDetail("12345", "1", "083000"),  // 08:30:00
            OrderDetail("12345", "2", "093000"),  // 09:30:00
            OrderDetail("12345", "3", "103000"),  // 10:30:00
            OrderDetail("12345", "4", "083500")   // 08:35:00
        )

        val nineAM = LocalTime.of(9, 0, 0)

        // When
        val afternoonOrders = orders.filter { order ->
            order.getOrderTime()?.isAfter(nineAM) ?: false
        }

        // Then
        assertEquals(2, afternoonOrders.size)
        assertTrue(afternoonOrders.any { it.orderNumber == "2" })
        assertTrue(afternoonOrders.any { it.orderNumber == "3" })
    }
}


package com.trueedu.spac.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * DateTimeUtils 유닛 테스트
 */
class DateTimeUtilsTest {

    // ========================================
    // toLocalDate() 테스트
    // ========================================

    @Test
    fun `toLocalDate - yyyy-MM-dd 형식 파싱 성공`() {
        // Given
        val dateString = "2025-10-12"

        // When
        val result = dateString.toLocalDate()

        // Then
        assertNotNull(result)
        assertEquals(2025, result?.year)
        assertEquals(10, result?.monthValue)
        assertEquals(12, result?.dayOfMonth)
    }

    @Test
    fun `toLocalDate - yyyyMMdd 형식 파싱 성공`() {
        // Given
        val dateString = "20251012"

        // When
        val result = dateString.toLocalDate()

        // Then
        assertNotNull(result)
        assertEquals(2025, result?.year)
        assertEquals(10, result?.monthValue)
        assertEquals(12, result?.dayOfMonth)
    }

    @Test
    fun `toLocalDate - 빈 문자열은 null 반환`() {
        // Given
        val emptyString = ""

        // When
        val result = emptyString.toLocalDate()

        // Then
        assertNull(result)
    }

    @Test
    fun `toLocalDate - 공백 문자열은 null 반환`() {
        // Given
        val blankString = "   "

        // When
        val result = blankString.toLocalDate()

        // Then
        assertNull(result)
    }

    @Test
    fun `toLocalDate - 잘못된 형식은 null 반환`() {
        // Given
        val invalidString = "2025/10/12"

        // When
        val result = invalidString.toLocalDate()

        // Then
        assertNull(result)
    }

    @Test
    fun `toLocalDate - 잘못된 날짜는 null 반환`() {
        // Given
        val invalidDate = "2025-13-32"

        // When
        val result = invalidDate.toLocalDate()

        // Then
        assertNull(result)
    }

    // ========================================
    // toLocalDateTime() 테스트
    // ========================================

    @Test
    fun `toLocalDateTime - yyyy-MM-dd HH_mm_ss 형식 파싱 성공`() {
        // Given
        val dateTimeString = "2025-10-12 01:30:45"

        // When
        val result = dateTimeString.toLocalDateTime()

        // Then
        assertNotNull(result)
        assertEquals(2025, result?.year)
        assertEquals(10, result?.monthValue)
        assertEquals(12, result?.dayOfMonth)
        assertEquals(1, result?.hour)
        assertEquals(30, result?.minute)
        assertEquals(45, result?.second)
    }

    @Test
    fun `toLocalDateTime - yyyyMMddHHmmss 형식 파싱 성공`() {
        // Given
        val dateTimeString = "20251012013045"

        // When
        val result = dateTimeString.toLocalDateTime()

        // Then
        assertNotNull(result)
        assertEquals(2025, result?.year)
        assertEquals(10, result?.monthValue)
        assertEquals(12, result?.dayOfMonth)
        assertEquals(1, result?.hour)
        assertEquals(30, result?.minute)
        assertEquals(45, result?.second)
    }

    @Test
    fun `toLocalDateTime - 빈 문자열은 null 반환`() {
        // Given
        val emptyString = ""

        // When
        val result = emptyString.toLocalDateTime()

        // Then
        assertNull(result)
    }

    @Test
    fun `toLocalDateTime - 잘못된 형식은 null 반환`() {
        // Given
        val invalidString = "2025/10/12 01:30:45"

        // When
        val result = invalidString.toLocalDateTime()

        // Then
        assertNull(result)
    }

    // ========================================
    // toLocalTime() 테스트
    // ========================================

    @Test
    fun `toLocalTime - HH_mm_ss 형식 파싱 성공`() {
        // Given
        val timeString = "01:30:45"

        // When
        val result = timeString.toLocalTime()

        // Then
        assertNotNull(result)
        assertEquals(1, result?.hour)
        assertEquals(30, result?.minute)
        assertEquals(45, result?.second)
    }

    @Test
    fun `toLocalTime - HHmmss 형식 파싱 성공`() {
        // Given
        val timeString = "013045"

        // When
        val result = timeString.toLocalTime()

        // Then
        assertNotNull(result)
        assertEquals(1, result?.hour)
        assertEquals(30, result?.minute)
        assertEquals(45, result?.second)
    }

    @Test
    fun `toLocalTime - 빈 문자열은 null 반환`() {
        // Given
        val emptyString = ""

        // When
        val result = emptyString.toLocalTime()

        // Then
        assertNull(result)
    }

    @Test
    fun `toLocalTime - 잘못된 형식은 null 반환`() {
        // Given
        val invalidString = "1:30:45"

        // When
        val result = invalidString.toLocalTime()

        // Then
        assertNull(result)
    }

    @Test
    fun `toLocalTime - 잘못된 시간은 null 반환`() {
        // Given
        val invalidTime = "25:70:80"

        // When
        val result = invalidTime.toLocalTime()

        // Then
        assertNull(result)
    }

    // ========================================
    // LocalDate to String 변환 테스트
    // ========================================

    @Test
    fun `toDateString - LocalDate를 yyyy-MM-dd 형식으로 변환`() {
        // Given
        val date = LocalDate.of(2025, 10, 12)

        // When
        val result = date.toDateString()

        // Then
        assertEquals("2025-10-12", result)
    }

    @Test
    fun `toDateCompactString - LocalDate를 yyyyMMdd 형식으로 변환`() {
        // Given
        val date = LocalDate.of(2025, 10, 12)

        // When
        val result = date.toDateCompactString()

        // Then
        assertEquals("20251012", result)
    }

    // ========================================
    // LocalDateTime to String 변환 테스트
    // ========================================

    @Test
    fun `toDateTimeString - LocalDateTime을 yyyy-MM-dd HH_mm_ss 형식으로 변환`() {
        // Given
        val dateTime = LocalDateTime.of(2025, 10, 12, 1, 30, 45)

        // When
        val result = dateTime.toDateTimeString()

        // Then
        assertEquals("2025-10-12 01:30:45", result)
    }

    @Test
    fun `toDateTimeCompactString - LocalDateTime을 yyyyMMddHHmmss 형식으로 변환`() {
        // Given
        val dateTime = LocalDateTime.of(2025, 10, 12, 1, 30, 45)

        // When
        val result = dateTime.toDateTimeCompactString()

        // Then
        assertEquals("20251012013045", result)
    }

    // ========================================
    // LocalTime to String 변환 테스트
    // ========================================

    @Test
    fun `toTimeString - LocalTime을 HH_mm_ss 형식으로 변환`() {
        // Given
        val time = LocalTime.of(1, 30, 45)

        // When
        val result = time.toTimeString()

        // Then
        assertEquals("01:30:45", result)
    }

    @Test
    fun `toTimeCompactString - LocalTime을 HHmmss 형식으로 변환`() {
        // Given
        val time = LocalTime.of(1, 30, 45)

        // When
        val result = time.toTimeCompactString()

        // Then
        assertEquals("013045", result)
    }

    // ========================================
    // 양방향 변환 테스트
    // ========================================

    @Test
    fun `날짜 양방향 변환 - dash 형식`() {
        // Given
        val originalString = "2025-10-12"

        // When
        val date = originalString.toLocalDate()
        val backToString = date?.toDateString()

        // Then
        assertEquals(originalString, backToString)
    }

    @Test
    fun `날짜 양방향 변환 - compact 형식`() {
        // Given
        val originalString = "20251012"

        // When
        val date = originalString.toLocalDate()
        val backToString = date?.toDateCompactString()

        // Then
        assertEquals(originalString, backToString)
    }

    @Test
    fun `날짜시간 양방향 변환 - dash 형식`() {
        // Given
        val originalString = "2025-10-12 01:30:45"

        // When
        val dateTime = originalString.toLocalDateTime()
        val backToString = dateTime?.toDateTimeString()

        // Then
        assertEquals(originalString, backToString)
    }

    @Test
    fun `날짜시간 양방향 변환 - compact 형식`() {
        // Given
        val originalString = "20251012013045"

        // When
        val dateTime = originalString.toLocalDateTime()
        val backToString = dateTime?.toDateTimeCompactString()

        // Then
        assertEquals(originalString, backToString)
    }

    @Test
    fun `시간 양방향 변환 - colon 형식`() {
        // Given
        val originalString = "01:30:45"

        // When
        val time = originalString.toLocalTime()
        val backToString = time?.toTimeString()

        // Then
        assertEquals(originalString, backToString)
    }

    @Test
    fun `시간 양방향 변환 - compact 형식`() {
        // Given
        val originalString = "013045"

        // When
        val time = originalString.toLocalTime()
        val backToString = time?.toTimeCompactString()

        // Then
        assertEquals(originalString, backToString)
    }

    // ========================================
    // Edge Case 테스트
    // ========================================

    @Test
    fun `윤년 날짜 파싱`() {
        // Given
        val leapYearDate = "2024-02-29"

        // When
        val result = leapYearDate.toLocalDate()

        // Then
        assertNotNull(result)
        assertEquals(2024, result?.year)
        assertEquals(2, result?.monthValue)
        assertEquals(29, result?.dayOfMonth)
    }


    @Test
    fun `자정 시간 파싱`() {
        // Given
        val midnight = "00:00:00"

        // When
        val result = midnight.toLocalTime()

        // Then
        assertNotNull(result)
        assertEquals(0, result?.hour)
        assertEquals(0, result?.minute)
        assertEquals(0, result?.second)
    }

    @Test
    fun `23시 59분 59초 파싱`() {
        // Given
        val lastSecond = "23:59:59"

        // When
        val result = lastSecond.toLocalTime()

        // Then
        assertNotNull(result)
        assertEquals(23, result?.hour)
        assertEquals(59, result?.minute)
        assertEquals(59, result?.second)
    }
}


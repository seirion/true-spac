package com.trueedu.spac.api.model.dto.price

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

/**
 * PriceChangeSign Enum 유닛 테스트
 */
class PriceChangeSignTest {

    @Test
    fun `from - 올바른 코드를 PriceChangeSign으로 변환`() {
        // When & Then
        assertEquals(PriceChangeSign.UPPER_LIMIT, PriceChangeSign.from("1"))
        assertEquals(PriceChangeSign.RISE, PriceChangeSign.from("2"))
        assertEquals(PriceChangeSign.UNCHANGED, PriceChangeSign.from("3"))
        assertEquals(PriceChangeSign.LOWER_LIMIT, PriceChangeSign.from("4"))
        assertEquals(PriceChangeSign.FALL, PriceChangeSign.from("5"))
    }

    @Test
    fun `from - 알 수 없는 코드는 UNKNOWN 반환`() {
        // When
        val result1 = PriceChangeSign.from("0")
        val result2 = PriceChangeSign.from("9")
        val result3 = PriceChangeSign.from("")
        val result4 = PriceChangeSign.from("invalid")

        // Then
        assertEquals(PriceChangeSign.UNKNOWN, result1)
        assertEquals(PriceChangeSign.UNKNOWN, result2)
        assertEquals(PriceChangeSign.UNKNOWN, result3)
        assertEquals(PriceChangeSign.UNKNOWN, result4)
    }

    @Test
    fun `code 프로퍼티가 올바른 값을 반환`() {
        // Then
        assertEquals("1", PriceChangeSign.UPPER_LIMIT.code)
        assertEquals("2", PriceChangeSign.RISE.code)
        assertEquals("3", PriceChangeSign.UNCHANGED.code)
        assertEquals("4", PriceChangeSign.LOWER_LIMIT.code)
        assertEquals("5", PriceChangeSign.FALL.code)
    }

    @Test
    fun `description 프로퍼티가 올바른 값을 반환`() {
        // Then
        assertEquals("상한", PriceChangeSign.UPPER_LIMIT.description)
        assertEquals("상승", PriceChangeSign.RISE.description)
        assertEquals("보합", PriceChangeSign.UNCHANGED.description)
        assertEquals("하한", PriceChangeSign.LOWER_LIMIT.description)
        assertEquals("하락", PriceChangeSign.FALL.description)
    }

    @Test
    fun `isPositive - 상한과 상승은 true 반환`() {
        // Then
        assertTrue(PriceChangeSign.UPPER_LIMIT.isPositive())
        assertTrue(PriceChangeSign.RISE.isPositive())
        assertFalse(PriceChangeSign.UNCHANGED.isPositive())
        assertFalse(PriceChangeSign.LOWER_LIMIT.isPositive())
        assertFalse(PriceChangeSign.FALL.isPositive())
    }

    @Test
    fun `isNegative - 하한과 하락은 true 반환`() {
        // Then
        assertFalse(PriceChangeSign.UPPER_LIMIT.isNegative())
        assertFalse(PriceChangeSign.RISE.isNegative())
        assertFalse(PriceChangeSign.UNCHANGED.isNegative())
        assertTrue(PriceChangeSign.LOWER_LIMIT.isNegative())
        assertTrue(PriceChangeSign.FALL.isNegative())
    }

    @Test
    fun `isUnchanged - 보합만 true 반환`() {
        // Then
        assertFalse(PriceChangeSign.UPPER_LIMIT.isUnchanged())
        assertFalse(PriceChangeSign.RISE.isUnchanged())
        assertTrue(PriceChangeSign.UNCHANGED.isUnchanged())
        assertFalse(PriceChangeSign.LOWER_LIMIT.isUnchanged())
        assertFalse(PriceChangeSign.FALL.isUnchanged())
    }

    @Test
    fun `PriceChangeSignSerializer - serialization 테스트`() {
        // Given
        val json = Json
        val sign = PriceChangeSign.RISE

        // When
        val serialized = json.encodeToString(PriceChangeSignSerializer, sign)

        // Then
        assertEquals("\"2\"", serialized)
    }

    @Test
    fun `PriceChangeSignSerializer - deserialization 테스트`() {
        // Given
        val json = Json
        val jsonString = "\"4\""

        // When
        val deserialized = json.decodeFromString(PriceChangeSignSerializer, jsonString)

        // Then
        assertEquals(PriceChangeSign.LOWER_LIMIT, deserialized)
    }

    @Test
    fun `PriceChangeSignSerializer - 알 수 없는 코드 deserialization`() {
        // Given
        val json = Json
        val jsonString = "\"9\""

        // When
        val deserialized = json.decodeFromString(PriceChangeSignSerializer, jsonString)

        // Then
        assertEquals(PriceChangeSign.UNKNOWN, deserialized)
    }

    @Test
    fun `PriceChangeSignSerializer - 양방향 변환 테스트`() {
        // Given
        val json = Json
        val originalSign = PriceChangeSign.UPPER_LIMIT

        // When
        val serialized = json.encodeToString(PriceChangeSignSerializer, originalSign)
        val deserialized = json.decodeFromString(PriceChangeSignSerializer, serialized)

        // Then
        assertEquals(originalSign, deserialized)
    }

    @Test
    fun `모든 PriceChangeSign entries 순회 테스트`() {
        // When
        val allSigns = PriceChangeSign.entries

        // Then
        assertEquals(6, allSigns.size)
        assertTrue(allSigns.contains(PriceChangeSign.UPPER_LIMIT))
        assertTrue(allSigns.contains(PriceChangeSign.UNKNOWN))
    }

    @Test
    fun `실제 사용 시나리오 - 가격 변동 색상 결정`() {
        // Given
        val riseSign = PriceChangeSign.RISE
        val fallSign = PriceChangeSign.FALL
        val unchangedSign = PriceChangeSign.UNCHANGED

        // When
        val riseColor = if (riseSign.isPositive()) "red" else if (riseSign.isNegative()) "blue" else "gray"
        val fallColor = if (fallSign.isPositive()) "red" else if (fallSign.isNegative()) "blue" else "gray"
        val unchangedColor = if (unchangedSign.isUnchanged()) "gray" else "unknown"

        // Then
        assertEquals("red", riseColor)
        assertEquals("blue", fallColor)
        assertEquals("gray", unchangedColor)
    }

    @Test
    fun `실제 사용 시나리오 - when 표현식에서 사용`() {
        // Given
        val sign = PriceChangeSign.UPPER_LIMIT

        // When
        val message = when (sign) {
            PriceChangeSign.UPPER_LIMIT -> "상한가"
            PriceChangeSign.LOWER_LIMIT -> "하한가"
            PriceChangeSign.RISE -> "상승"
            PriceChangeSign.FALL -> "하락"
            PriceChangeSign.UNCHANGED -> "보합"
            PriceChangeSign.UNKNOWN -> "알 수 없음"
        }

        // Then
        assertEquals("상한가", message)
    }

    @Test
    fun `실제 사용 시나리오 - 상승 종목 필터링`() {
        // Given
        val signs = listOf(
            PriceChangeSign.UPPER_LIMIT,
            PriceChangeSign.RISE,
            PriceChangeSign.UNCHANGED,
            PriceChangeSign.FALL,
            PriceChangeSign.LOWER_LIMIT
        )

        // When
        val positiveSigns = signs.filter { it.isPositive() }

        // Then
        assertEquals(2, positiveSigns.size)
        assertTrue(positiveSigns.contains(PriceChangeSign.UPPER_LIMIT))
        assertTrue(positiveSigns.contains(PriceChangeSign.RISE))
    }
}


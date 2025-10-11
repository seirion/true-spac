package com.trueedu.spac.api.model.dto.price

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.Assert.*
import org.junit.Test

/**
 * StockState Enum 유닛 테스트
 */
class StockStateTest {

    @Test
    fun `from - 정상 코드를 StockState로 변환`() {
        // When & Then
        assertEquals(StockState.NORMAL, StockState.from("00"))
        assertEquals(StockState.MANAGED, StockState.from("51"))
        assertEquals(StockState.INVESTMENT_RISK, StockState.from("52"))
        assertEquals(StockState.INVESTMENT_WARNING, StockState.from("53"))
        assertEquals(StockState.INVESTMENT_CAUTION, StockState.from("54"))
        assertEquals(StockState.CREDIT_AVAILABLE, StockState.from("55"))
        assertEquals(StockState.MARGIN_100, StockState.from("57"))
        assertEquals(StockState.TRADING_HALT, StockState.from("58"))
        assertEquals(StockState.SHORT_TERM_OVERHEATING, StockState.from("59"))
    }

    @Test
    fun `from - 알 수 없는 코드는 UNKNOWN 반환`() {
        // When
        val result1 = StockState.from("99")
        val result2 = StockState.from("unknown")
        val result3 = StockState.from("")

        // Then
        assertEquals(StockState.UNKNOWN, result1)
        assertEquals(StockState.UNKNOWN, result2)
        assertEquals(StockState.UNKNOWN, result3)
    }

    @Test
    fun `code 프로퍼티가 올바른 값을 반환`() {
        // Then
        assertEquals("00", StockState.NORMAL.code)
        assertEquals("51", StockState.MANAGED.code)
        assertEquals("58", StockState.TRADING_HALT.code)
    }

    @Test
    fun `description 프로퍼티가 올바른 값을 반환`() {
        // Then
        assertEquals("정상", StockState.NORMAL.description)
        assertEquals("관리종목", StockState.MANAGED.description)
        assertEquals("거래정지", StockState.TRADING_HALT.description)
    }

    @Test
    fun `StockStateSerializer - serialization 테스트`() {
        // Given
        val json = Json
        val stockState = StockState.TRADING_HALT

        // When
        val serialized = json.encodeToString(StockStateSerializer, stockState)

        // Then
        assertEquals("\"58\"", serialized)
    }

    @Test
    fun `StockStateSerializer - deserialization 테스트`() {
        // Given
        val json = Json
        val jsonString = "\"51\""

        // When
        val deserialized = json.decodeFromString(StockStateSerializer, jsonString)

        // Then
        assertEquals(StockState.MANAGED, deserialized)
    }

    @Test
    fun `StockStateSerializer - 알 수 없는 코드 deserialization`() {
        // Given
        val json = Json
        val jsonString = "\"99\""

        // When
        val deserialized = json.decodeFromString(StockStateSerializer, jsonString)

        // Then
        assertEquals(StockState.UNKNOWN, deserialized)
    }

    @Test
    fun `StockStateSerializer - 양방향 변환 테스트`() {
        // Given
        val json = Json
        val originalState = StockState.INVESTMENT_WARNING

        // When
        val serialized = json.encodeToString(StockStateSerializer, originalState)
        val deserialized = json.decodeFromString(StockStateSerializer, serialized)

        // Then
        assertEquals(originalState, deserialized)
    }

    @Test
    fun `모든 StockState entries 순회 테스트`() {
        // When
        val allStates = StockState.entries

        // Then
        assertEquals(10, allStates.size)
        assertTrue(allStates.contains(StockState.NORMAL))
        assertTrue(allStates.contains(StockState.UNKNOWN))
    }

    @Test
    fun `실제 사용 시나리오 - 거래 가능 여부 판단`() {
        // Given
        val normalStock = StockState.NORMAL
        val haltedStock = StockState.TRADING_HALT
        val managedStock = StockState.MANAGED

        // Then
        assertTrue(normalStock == StockState.NORMAL)
        assertTrue(haltedStock == StockState.TRADING_HALT)
        assertTrue(managedStock == StockState.MANAGED)
    }

    @Test
    fun `실제 사용 시나리오 - when 표현식에서 사용`() {
        // Given
        val state = StockState.TRADING_HALT

        // When
        val message = when (state) {
            StockState.TRADING_HALT -> "거래 불가능"
            StockState.MANAGED -> "주의 필요"
            StockState.NORMAL -> "정상 거래 가능"
            else -> "확인 필요"
        }

        // Then
        assertEquals("거래 불가능", message)
    }
}


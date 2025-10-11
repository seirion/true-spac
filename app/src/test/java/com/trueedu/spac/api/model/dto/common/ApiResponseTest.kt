package com.trueedu.spac.api.model.dto.common

import com.trueedu.spac.api.model.dto.price.PriceDetail
import com.trueedu.spac.api.model.dto.price.PriceResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Assert.*
import org.junit.Test

/**
 * ApiResponse 인터페이스 유닛 테스트
 */
class ApiResponseTest {

    /**
     * 테스트용 ApiResponse 구현체
     */
    @Serializable
    data class TestApiResponse(
        @SerialName("rt_cd")
        override val rtCd: String,
        @SerialName("msg_cd")
        override val msgCd: String,
        override val msg1: String?,
        val testData: String? = null
    ) : ApiResponse

    @Test
    fun `isSuccess - 성공 코드일 때 true 반환`() {
        // Given
        val successResponse = TestApiResponse(
            rtCd = "0",
            msgCd = "TEST0000",
            msg1 = "정상처리 되었습니다."
        )

        // When & Then
        assertTrue(successResponse.isSuccess())
    }

    @Test
    fun `isSuccess - 실패 코드일 때 false 반환`() {
        // Given
        val failureResponse = TestApiResponse(
            rtCd = "1",
            msgCd = "ERROR0001",
            msg1 = "오류가 발생했습니다."
        )

        // When & Then
        assertFalse(failureResponse.isSuccess())
    }

    @Test
    fun `isFailure - 실패 코드일 때 true 반환`() {
        // Given
        val failureResponse = TestApiResponse(
            rtCd = "1",
            msgCd = "ERROR0001",
            msg1 = "오류가 발생했습니다."
        )

        // When & Then
        assertTrue(failureResponse.isFailure())
    }

    @Test
    fun `isFailure - 성공 코드일 때 false 반환`() {
        // Given
        val successResponse = TestApiResponse(
            rtCd = "0",
            msgCd = "TEST0000",
            msg1 = "정상처리 되었습니다."
        )

        // When & Then
        assertFalse(successResponse.isFailure())
    }

    @Test
    fun `RT_CD_SUCCESS 상수값 확인`() {
        // Then
        assertEquals("0", ApiResponse.RT_CD_SUCCESS)
    }

    @Test
    fun `RT_CD_FAILURE 상수값 확인`() {
        // Then
        assertEquals("1", ApiResponse.RT_CD_FAILURE)
    }

    @Test
    fun `msg1이 null이어도 isSuccess 동작`() {
        // Given
        val response = TestApiResponse(
            rtCd = "0",
            msgCd = "TEST0000",
            msg1 = null
        )

        // When & Then
        assertTrue(response.isSuccess())
        assertEquals("TEST0000", response.msgCd)
    }

    @Test
    fun `실제 Response 클래스 - PriceResponse isSuccess 테스트`() {
        // Given
        val successResponse = PriceResponse(
            output = null,
            rtCd = "0",
            msgCd = "MCA00000",
            msg1 = "정상처리 되었습니다."
        )

        val failureResponse = PriceResponse(
            output = null,
            rtCd = "1",
            msgCd = "ERROR0001",
            msg1 = "오류가 발생했습니다."
        )

        // When & Then
        assertTrue(successResponse.isSuccess())
        assertFalse(successResponse.isFailure())

        assertFalse(failureResponse.isSuccess())
        assertTrue(failureResponse.isFailure())
    }

    @Test
    fun `실제 사용 시나리오 - 응답 처리`() {
        // Given
        val response = TestApiResponse(
            rtCd = "0",
            msgCd = "TEST0000",
            msg1 = "성공",
            testData = "result"
        )

        // When
        val result = if (response.isSuccess()) {
            "처리 성공: ${response.testData}"
        } else {
            "처리 실패: ${response.msg1}"
        }

        // Then
        assertEquals("처리 성공: result", result)
    }

    @Test
    fun `실제 사용 시나리오 - 공통 처리 함수`() {
        // Given
        var successCalled = false
        var failureCalled = false

        val successResponse = TestApiResponse("0", "TEST0000", "성공")
        val failureResponse = TestApiResponse("1", "ERROR0001", "실패")

        fun <T : ApiResponse> handleResponse(response: T, onSuccess: () -> Unit, onFailure: () -> Unit) {
            if (response.isSuccess()) onSuccess() else onFailure()
        }

        // When
        handleResponse(successResponse, { successCalled = true }, { failureCalled = true })
        val failedBefore = failureCalled
        handleResponse(failureResponse, { successCalled = false }, { failureCalled = true })

        // Then
        assertTrue(successCalled)
        assertFalse(failedBefore) // 첫 번째 호출에서는 실패 콜백이 호출되지 않음
        assertTrue(failureCalled) // 두 번째 호출에서는 실패 콜백이 호출됨
    }

    @Test
    fun `실제 사용 시나리오 - when 표현식으로 여러 응답 타입 처리`() {
        // Given
        val response: ApiResponse = TestApiResponse("0", "TEST0000", "성공")

        // When
        val message = when {
            response.isFailure() -> "오류: ${response.msg1}"
            response is PriceResponse -> "가격 데이터"
            response is TestApiResponse -> "테스트 데이터"
            else -> "알 수 없음"
        }

        // Then
        assertEquals("테스트 데이터", message)
    }

    @Test
    fun `실제 사용 시나리오 - 응답 리스트 필터링`() {
        // Given
        val responses = listOf(
            TestApiResponse("0", "TEST0000", "성공1"),
            TestApiResponse("1", "ERROR0001", "실패1"),
            TestApiResponse("0", "TEST0000", "성공2"),
            TestApiResponse("1", "ERROR0002", "실패2"),
            TestApiResponse("0", "TEST0000", "성공3")
        )

        // When
        val successResponses = responses.filter { it.isSuccess() }
        val failureResponses = responses.filter { it.isFailure() }

        // Then
        assertEquals(3, successResponses.size)
        assertEquals(2, failureResponses.size)
    }

    @Test
    fun `실제 사용 시나리오 - 에러 메시지 수집`() {
        // Given
        val responses = listOf(
            TestApiResponse("0", "TEST0000", "성공"),
            TestApiResponse("1", "ERROR0001", "오류1"),
            TestApiResponse("1", "ERROR0002", "오류2")
        )

        // When
        val errorMessages = responses
            .filter { it.isFailure() }
            .mapNotNull { it.msg1 }

        // Then
        assertEquals(2, errorMessages.size)
        assertTrue(errorMessages.contains("오류1"))
        assertTrue(errorMessages.contains("오류2"))
    }
}


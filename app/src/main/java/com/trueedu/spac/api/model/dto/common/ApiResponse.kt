package com.trueedu.spac.api.model.dto.common

/**
 * 한국투자증권 API 공통 응답 인터페이스
 */
interface ApiResponse {
    /**
     * 성공 실패 여부
     * "0": 성공
     * "1": 실패
     */
    val rtCd: String

    /**
     * 응답 코드
     * 예: "MCA00000", "KIOK0560" 등
     */
    val msgCd: String

    /**
     * 응답 메시지
     * 예: "정상처리 되었습니다."
     * 일부 API는 이 필드를 사용하지 않을 수 있습니다.
     */
    val msg1: String?

    /**
     * API 호출 성공 여부
     */
    fun isSuccess(): Boolean = rtCd == RT_CD_SUCCESS

    /**
     * API 호출 실패 여부
     */
    fun isFailure(): Boolean = !isSuccess()

    companion object {
        /**
         * 응답 성공 코드
         */
        const val RT_CD_SUCCESS = "0"

        /**
         * 응답 실패 코드
         */
        const val RT_CD_FAILURE = "1"
    }
}


package com.trueedu.spac.api.model.dto.auth

import kotlinx.serialization.Serializable

/**
 * https://apiportal.koreainvestment.com/apiservice/oauth2#L_dd3cb447-5034-4711-8c88-62c913429c7b
 *
 */
@Serializable
data class RevokeTokenResponse(
    val code: String, // HTTP 응답코드
    val message: String, // 응답 메시지
)

package com.trueedu.spac.api.model.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * https://apiportal.koreainvestment.com/apiservice/oauth2#L_fa778c98-f68d-451e-8fff-b1c6bfe5cd30
 *
 * access_token	접근토큰	String	Y	350	OAuth 토큰이 필요한 API 경우 발급한 Access token
 * ex) "eyJ0eXUxMiJ9.eyJz…..................................."
 *
 * - 일반개인고객/일반법인고객
 * . Access token 유효기간 1일
 * .. 일정시간(6시간) 이내에 재호출 시에는 직전 토큰값을 리턴
 * . OAuth 2.0의 Client Credentials Grant 절차를 준용
 *
 * - 제휴법인
 * . Access token 유효기간 3개월
 * . Refresh token 유효기간 1년
 * . OAuth 2.0의 Authorization Code Grant 절차를 준용
 * token_type	접근토큰유형	String	Y	20	접근토큰유형 : "Bearer"
 * ※ API 호출 시, 접근토큰유형 "Bearer" 입력. ex) "Bearer eyJ...."
 * expires_in	접근토큰 유효기간	Number	Y	10	유효기간(초)
 * ex) 7776000
 * access_token_token_expired	접근토큰 유효기간(일시표시)	String	Y	50	유효기간(년:월:일 시:분:초)
 * ex) "2022-08-30 08:10:10"
 */
@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("access_token_token_expired")
    val accessTokenTokenExpired: String,
)

package com.trueedu.spac.repo.kis.services

import com.trueedu.spac.api.model.dto.auth.ApprovalKeyResponse
import com.trueedu.spac.api.model.dto.auth.HashKeyRequest
import com.trueedu.spac.api.model.dto.auth.HashKeyResponse
import com.trueedu.spac.api.model.dto.auth.RevokeTokenRequest
import com.trueedu.spac.api.model.dto.auth.RevokeTokenResponse
import com.trueedu.spac.api.model.dto.auth.TokenRequest
import com.trueedu.spac.api.model.dto.auth.TokenResponse
import com.trueedu.spac.api.model.dto.auth.WebSocketKeyRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("oauth2/tokenP")
    suspend fun refreshToken(@Body request: TokenRequest): Response<TokenResponse>

    @POST("oauth2/revokeP")
    suspend fun revokeToken(@Body request: RevokeTokenRequest): Response<RevokeTokenResponse>

    @POST("oauth2/Approval")
    suspend fun webSocketKey(@Body request: WebSocketKeyRequest): Response<ApprovalKeyResponse>

    @POST("uapi/hashkey")
    suspend fun hashKey(@Body request: HashKeyRequest): Response<HashKeyResponse>
}

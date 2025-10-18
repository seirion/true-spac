package com.trueedu.spac.repo.kis

import com.trueedu.spac.api.model.dto.auth.ApprovalKeyResponse
import com.trueedu.spac.api.model.dto.auth.HashKeyRequest
import com.trueedu.spac.api.model.dto.auth.HashKeyResponse
import com.trueedu.spac.api.model.dto.auth.RevokeTokenRequest
import com.trueedu.spac.api.model.dto.auth.RevokeTokenResponse
import com.trueedu.spac.api.model.dto.auth.TokenRequest
import com.trueedu.spac.api.model.dto.auth.TokenResponse
import com.trueedu.spac.api.model.dto.auth.WebSocketKeyRequest
import kotlinx.coroutines.flow.Flow

interface AuthRemote {
    fun refreshToken(request: TokenRequest): Flow<TokenResponse>

    fun revokeToken(request: RevokeTokenRequest): Flow<RevokeTokenResponse>

    fun webSocketKey(request: WebSocketKeyRequest): Flow<ApprovalKeyResponse>

    fun hashKey(request: HashKeyRequest): Flow<HashKeyResponse>
}
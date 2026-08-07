package com.trueedu.spac.repo.kis

import com.trueedu.spac.api.model.dto.auth.HashKeyRequest
import com.trueedu.spac.api.model.dto.auth.RevokeTokenRequest
import com.trueedu.spac.api.model.dto.auth.TokenRequest
import com.trueedu.spac.api.model.dto.auth.WebSocketKeyRequest
import com.trueedu.spac.network.apiCallFlow
import com.trueedu.spac.repo.kis.services.AuthService

class AuthRemoteImpl(
    private val authService: AuthService
): AuthRemote {
    override fun refreshToken(request: TokenRequest) = apiCallFlow {
        authService.refreshToken(request)
    }

    override fun revokeToken(request: RevokeTokenRequest) = apiCallFlow {
        authService.revokeToken(request)
    }

    override fun webSocketKey(request: WebSocketKeyRequest) = apiCallFlow {
        authService.webSocketKey(request)
    }

    override fun hashKey(request: HashKeyRequest) = apiCallFlow {
        authService.hashKey(request)
    }
}

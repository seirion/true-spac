package com.trueedu.spac.data.user

import androidx.compose.runtime.mutableStateOf
import com.trueedu.spac.api.model.dto.auth.RevokeTokenRequest
import com.trueedu.spac.api.model.dto.auth.TokenRequest
import com.trueedu.spac.api.model.dto.auth.TokenResponse
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.repo.kis.AuthRemote
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.repo.local.UserKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenKeyManager @Inject constructor(
    private val local: Local,
    private val authRemote: AuthRemote,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val userKey = mutableStateOf<UserKey?>(null)

    // auth 관련 이벤트 구독을 위함
    private val event = MutableSharedFlow<TokenKeyEvent>(1)

    fun observeTokenKeyEvent(): Flow<TokenKeyEvent> {
        return event
    }

    init {
        userKey.value = local.getUserKey()
        if (userKey.value?.isValid() == true) {
            issueAccessToken()
        }
    }

    private fun hasValidToken(): Boolean {

        if (local.accessToken.isEmpty()) return false

        val tokenExpirationTime = Date(local.accessTokenExpiredAt)
        val calendar = Calendar.getInstance()
        calendar.time = tokenExpirationTime
        calendar.add(Calendar.MINUTE, -5) // 5 minutes to the token expiration time
        val bufferedExpirationTime = calendar.time
        val currentTime = Date()

        return bufferedExpirationTime.after(currentTime)
    }

    private fun issueAccessToken() {
        logD("issueAccessToken()")
        val appKey = userKey.value?.appKey
        val appSecret = userKey.value?.appSecret
        if (appKey.isNullOrEmpty() || appSecret.isNullOrEmpty()) {
            logD("appKey appSecret is empty")
            return
        }
        if (hasValidToken()) {
            logD("token is valid")
            scope.launch {
                event.emit(TokenOk)
            }
            return
        }

        val request = TokenRequest(
            grantType = "client_credentials",
            appKey = appKey,
            appSecret = appSecret,
        )

        authRemote.refreshToken(request)
            .catch {
                // service not available
                logD("failed to get AccessToken: $it")
                event.emit(TokenIssueFail)
            }
            .onEach {
                setAccessToken(it)
                event.emit(TokenIssued)
                logD("new token: $it")
            }
            .launchIn(scope)
    }

    private fun revokeToken() {
        val key = userKey.value ?: return
        val appKey = key.appKey ?: return
        val appSecret = key.appSecret ?: return

        if (local.accessToken.isEmpty()) {
            return
        }

        val request = RevokeTokenRequest(
            appKey = appKey,
            appSecret = appSecret,
            token = local.accessToken,
        )
        authRemote.revokeToken(request)
            .catch {
                logD("failed to revoke AccessToken: $it")
                // service not available
            }
            .onEach {
                event.emit(TokenRevoked)
                logD("revoke ok: $it")
            }
            .launchIn(scope)
    }

    fun clearToken() {
        local.setAccessToken(null)
    }

    fun setAccessToken(tokenResponse: TokenResponse) {
        local.setAccessToken(tokenResponse)
    }

    fun addUserKey(newKey: UserKey) {
        local.setUserKey(newKey)
        userKey.value = newKey

        // 키 정보가 갱신되면 토큰을 재발급 받아야 함
        clearToken()
        issueAccessToken()
    }

    fun deleteUserKey() {
        local.clearUserKey()
        userKey.value = null
        clearToken()
    }
}

package com.trueedu.spac.network

import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.user.TokenKeyManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Provider

/**
 * KIS API 토큰 만료 에러를 감지하고 자동으로 토큰을 갱신한 후 재시도하는 인터셉터
 *
 * KIS API는 HTTP 401이 아닌 200 OK 또는 500 Error와 함께 body에 에러 코드를 반환하므로
 * Authenticator 대신 Interceptor를 사용합니다.
 *
 * 토큰 만료 응답: {"rt_cd": "1", "msg_cd": "EGW00123", "msg1": "기간이 만료된 token 입니다."}
 *
 * 동시성 처리:
 * - 여러 요청이 동시에 토큰 만료를 감지하더라도 토큰 갱신은 한 번만 실행됩니다.
 * - 나머지 요청들은 첫 번째 갱신이 완료될 때까지 대기합니다.
 *
 * 순환 참조 방지:
 * - Provider<TokenKeyManager>를 사용하여 지연 주입(lazy injection)으로 순환 참조를 회피합니다.
 * - TokenKeyManager → AuthRemote → Retrofit → OkHttpClient → TokenRefreshInterceptor → TokenKeyManager
 */
class TokenRefreshInterceptor(
    private val tokenKeyManagerProvider: Provider<TokenKeyManager>,
    private val trueAnalytics: TrueAnalytics,
) : Interceptor {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    // 동시에 여러 요청이 실패해도 토큰 갱신은 한 번만 수행되도록 함
    private val mutex = Mutex()
    private var refreshDeferred: CompletableDeferred<Boolean>? = null

    @Serializable
    private data class ErrorResponse(
        @SerialName("rt_cd")
        val rtCd: String,
        @SerialName("msg_cd")
        val msgCd: String,
        val msg1: String? = null,
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 재시도 여부를 체크하기 위한 헤더 확인 (무한 재시도 방지)
        val retryCount = originalRequest.header(RETRY_HEADER)?.toIntOrNull() ?: 0
        if (retryCount >= MAX_RETRY_COUNT) {
            logD("Max retry count reached, returning response without retry")
            return chain.proceed(originalRequest)
        }

        val response = chain.proceed(originalRequest)

        // HTTP 200-299 또는 500 응답인 경우 body를 체크 (토큰 만료 에러 감지)
        // KIS API는 토큰 만료 시 200 또는 500으로 응답할 수 있음
        if (response.isSuccessful || response.code == 500) {
            return checkAndHandleTokenExpiration(chain, response, retryCount)
        }

        return response
    }

    private fun checkAndHandleTokenExpiration(
        chain: Interceptor.Chain,
        response: Response,
        currentRetryCount: Int
    ): Response {
        // 응답 body를 읽기 위해 복사
        val responseBody = response.body
        val bodyString = responseBody?.string() ?: return response

        // JSON 파싱 시도
        val errorResponse = try {
            json.decodeFromString<ErrorResponse>(bodyString)
        } catch (e: Exception) {
            // 파싱 실패 시 원래 응답 반환 (body를 다시 생성해야 함)
            return response.newBuilder()
                .body(bodyString.toResponseBody(responseBody.contentType()))
                .build()
        }

        // 토큰 만료 에러 체크 (rt_cd: "1", msg_cd: "EGW00123")
        if (errorResponse.rtCd == "1" && errorResponse.msgCd == "EGW00123") {
            logD("Token expired detected (msg: ${errorResponse.msg1}), attempting to refresh token")

            // 기존 응답 닫기
            response.close()

            // 동시성 제어: 여러 요청이 동시에 실패해도 토큰 갱신은 한 번만
            val refreshSuccess = runBlocking {
                // 현재 진행 중인 갱신 작업을 가져오거나 새로 시작
                val deferred = mutex.withLock {
                    if (refreshDeferred != null && !refreshDeferred!!.isCompleted) {
                        logD("Token refresh already in progress, waiting for completion...")
                        return@withLock refreshDeferred!!
                    }

                    // 새로운 토큰 갱신 작업 시작
                    logD("Starting new token refresh operation")
                    val newDeferred = CompletableDeferred<Boolean>()
                    refreshDeferred = newDeferred
                    newDeferred
                }

                // mutex 밖에서 실제 토큰 갱신 수행
                // (다른 스레드들이 deferred를 가져갈 수 있도록)
                if (!deferred.isCompleted) {
                    try {
                        val tokenKeyManager = tokenKeyManagerProvider.get()
                        val result = tokenKeyManager.refreshTokenSync()
                        deferred.complete(result)
                        logD("Token refresh completed: $result")
                        if (result) {
                            trueAnalytics.log("token_refresh")
                        }
                    } catch (e: Exception) {
                        logD("Token refresh failed with exception: ${e.message}")
                        deferred.complete(false)
                    } finally {
                        // 완료 후 정리
                        mutex.withLock {
                            if (refreshDeferred == deferred) {
                                refreshDeferred = null
                            }
                        }
                    }
                }

                // 결과 대기 및 반환
                deferred.await()
            }

            if (refreshSuccess) {
                logD("Token refreshed successfully, retrying original request")

                // 재시도 횟수 증가
                val newRequest = chain.request().newBuilder()
                    .header(RETRY_HEADER, (currentRetryCount + 1).toString())
                    .build()

                return chain.proceed(newRequest)
            } else {
                logD("Token refresh failed, returning error response")
                // 토큰 갱신 실패 시 원래 에러 응답 반환
                return Response.Builder()
                    .request(chain.request())
                    .protocol(response.protocol)
                    .code(response.code)
                    .message(response.message)
                    .body(bodyString.toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }

        // 토큰 만료가 아니면 원래 응답 반환 (body를 다시 생성)
        return response.newBuilder()
            .body(bodyString.toResponseBody(responseBody.contentType()))
            .build()
    }

    companion object {
        private const val RETRY_HEADER = "X-Token-Retry-Count"
        private const val MAX_RETRY_COUNT = 1 // 최대 1번만 재시도
    }
}


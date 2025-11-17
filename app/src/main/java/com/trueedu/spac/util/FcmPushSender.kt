package com.trueedu.spac.util

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import com.trueedu.spac.BuildConfig
import com.trueedu.spac.data.log.logD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * ⚠️ 테스트 전용 FCM 푸시 전송 유틸리티
 * 주의: 프로덕션에서는 절대 사용하지 마세요!
 * 디버그 빌드에서만 동작합니다.
 *
 * Firebase HTTP v1 API를 사용합니다.
 */
object FcmPushSender {

    private const val SCOPES = "https://www.googleapis.com/auth/firebase.messaging"
    private const val SERVICE_ACCOUNT_FILE = "firebase-service-account.json"

    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    // 토큰 캐싱 (OAuth2 토큰은 1시간 유효)
    private var cachedToken: String? = null
    private var tokenExpiryTime: Long = 0
    private var cachedProjectId: String? = null

    @Serializable
    data class FcmV1Message(
        val message: Message
    )

    @Serializable
    data class Message(
        val token: String,
        val data: Map<String, String>,
        val android: AndroidConfig? = null
    )

    @Serializable
    data class AndroidConfig(
        val priority: String = "high"
    )

    /**
     * FCM 푸시 메시지 전송 (HTTP v1 API)
     *
     * @param context Android Context (assets에서 JSON 파일 읽기 위함)
     * @param token 수신자의 FCM 토큰
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택)
     * @param deepLink 딥링크 URL (선택, 알림 클릭 시 이동할 화면)
     * @return 성공 여부
     */
    suspend fun sendPush(
        context: Context,
        token: String,
        title: String,
        body: String,
        data: Map<String, String>? = null,
        deepLink: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        // 디버그 빌드가 아니면 실행 차단
        if (!BuildConfig.DEBUG) {
            logD("⚠️ FcmPushSender는 디버그 빌드에서만 사용 가능합니다")
            return@withContext Result.failure(
                IllegalStateException("FcmPushSender is only available in debug builds")
            )
        }

        try {
            // 프로젝트 ID 및 OAuth2 액세스 토큰 가져오기
            val projectId = getProjectId(context)
            val accessToken = getAccessToken(context)
            logD("🔑 OAuth2 토큰 획득 완료 (프로젝트: $projectId)")

            val fcmUrl = "https://fcm.googleapis.com/v1/projects/$projectId/messages:send"

            // data 필드에 title, body, deepLink 포함
            val messageData = buildMap {
                put("title", title)
                put("body", body)
                deepLink?.let { put("deepLink", it) }
                data?.forEach { (key, value) -> put(key, value) }
            }

            val message = FcmV1Message(
                message = Message(
                    token = token,
                    data = messageData,
                    android = AndroidConfig(
                        priority = "high"
                    )
                )
            )

            val jsonBody = json.encodeToString(message)
            logD("📤 FCM v1 전송 시작: $title")

            val requestBody = jsonBody.toRequestBody(
                "application/json; charset=utf-8".toMediaType()
            )

            val request = Request.Builder()
                .url(fcmUrl)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                logD("✅ FCM v1 전송 성공: $responseBody")
                Result.success(responseBody)
            } else {
                logD("❌ FCM v1 전송 실패: ${response.code} - $responseBody")
                Result.failure(Exception("FCM Error: ${response.code} - $responseBody"))
            }
        } catch (e: Exception) {
            logD("❌ FCM v1 전송 오류: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Firebase Admin SDK JSON 파일에서 프로젝트 ID 읽기
     */
    private fun getProjectId(context: Context): String {
        cachedProjectId?.let { return it }

        return context.assets.open(SERVICE_ACCOUNT_FILE).use { stream ->
            val jsonString = stream.bufferedReader().use { it.readText() }
            val jsonObject = json.parseToJsonElement(jsonString).jsonObject
            val projectId = jsonObject["project_id"]?.jsonPrimitive?.content
                ?: throw IllegalStateException("project_id not found in service account file")
            cachedProjectId = projectId
            projectId
        }
    }

    /**
     * Firebase Admin SDK JSON 파일에서 OAuth2 액세스 토큰 생성
     * 토큰이 캐싱되어 있고 아직 유효하면 캐시된 토큰을 반환합니다.
     */
    private fun getAccessToken(context: Context): String {
        // 캐시된 토큰이 있고 아직 유효하면 반환
        val now = System.currentTimeMillis()
        if (cachedToken != null && now < tokenExpiryTime) {
            logD("🔄 캐시된 토큰 사용 (만료까지 ${(tokenExpiryTime - now) / 1000}초)")
            return cachedToken!!
        }

        // 새로운 토큰 생성
        return context.assets.open(SERVICE_ACCOUNT_FILE).use { stream ->
            val credentials = GoogleCredentials
                .fromStream(stream)
                .createScoped(listOf(SCOPES))

            credentials.refreshIfExpired()

            val token = credentials.accessToken.tokenValue
            val expiryDate = credentials.accessToken.expirationTime?.time
                ?: (now + 3600 * 1000) // 기본값: 1시간

            // 토큰 캐싱 (만료 5분 전까지 사용)
            cachedToken = token
            tokenExpiryTime = expiryDate - (5 * 60 * 1000)

            logD("🆕 새 토큰 생성 (유효 시간: ${(tokenExpiryTime - now) / 1000}초)")
            token
        }
    }
}

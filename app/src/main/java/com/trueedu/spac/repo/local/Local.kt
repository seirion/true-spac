package com.trueedu.spac.repo.local

import android.content.SharedPreferences
import androidx.compose.runtime.staticCompositionLocalOf
import com.trueedu.spac.api.model.dto.auth.TokenResponse
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.util.toLocalDateTime
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.ZoneOffset
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

val LocalTrueLocal = staticCompositionLocalOf<Local> {
    error("No Local provided")
}

@Singleton
class Local @Inject constructor(
    private val preferences: SharedPreferences
) {
    private val latestVersion = 1
    private var currentVersion by preferences.int(latestVersion)

    var launchingCount by preferences.long(0) // 앱 실행 횟수
        private set

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun migrate() {
        launchingCount++
    }

    // dart 정보
    var dartApiKey by preferences.string("")

    // appKey, appSecret, accountNumber, htsId
    var userKey by preferences.string("{}")
        private set

    fun setUserKey(userKey: UserKey) {
        this.userKey = try {
            json.encodeToString(userKey)
        } catch (e: SerializationException) {
            logD("Failed to serialize UserKey: ${e.message}")
            "{}"
        }
    }

    fun clearUserKey() {
        this.userKey = "{}"
    }

    fun getUserKey(): UserKey {
        return try {
            json.decodeFromString<UserKey>(userKey)
        } catch (e: SerializationException) {
            UserKey(null, null, null, null)
        }
    }

    var accessToken by preferences.string("")
        private set
    // 토큰 만료 예정 시각 - Date.time 값 저장
    var accessTokenExpiredAt by preferences.long(0L)
        private set

    fun setAccessToken(tokenResponse: TokenResponse?) {
        if (tokenResponse == null) {
            accessToken = ""
            accessTokenExpiredAt = 0L
        } else {
            accessToken = tokenResponse.accessToken
            val expiredTime = tokenResponse.accessTokenTokenExpired
                .toLocalDateTime()
                ?.toInstant(ZoneOffset.of("+09:00"))
                ?.toEpochMilli()
                ?: 0L
            accessTokenExpiredAt = expiredTime
        }
    }

    var deviceId by preferences.string(UUID.randomUUID().toString()) // 고유 id
        private set

    var notificationToken by preferences.string("")

    // user
    var email by preferences.string("")
    var profileImageUrl by preferences.string("")

    // 확인한 notice 마지막 id
    var appNoticeId by preferences.int(0)

    // 면책 문구 확인 여부
    var disclaimerAccepted by preferences.boolean(false)

    // 종목 다운로드 시각 yyyyMMddHHmm
    var stockUpdatedAt by preferences.long(0L)

    var priceUpdatedAt by preferences.long(0L)

    // 스팩 설정
    var spacAnnualProfit by preferences.boolean(false) // 청산 가치 1년 환산 표시

    // 화면 항상 켜기
    var keepScreenOn by preferences.boolean(false)

    // 검색 기록 (최대 10개 저장)
    private var searchHistoryList by preferences.stringList(emptyList())
    private val maxSearchHistory = 10

    fun getSearchHistory(): List<String> {
        return searchHistoryList
    }

    fun addSearchHistory(query: String) {
        if (query.isBlank()) return

        val trimmedQuery = query.trim()
        val currentHistory = searchHistoryList.toMutableList()

        // 기존 항목 제거 (중복 방지)
        currentHistory.remove(trimmedQuery)

        // 맨 앞에 추가 (최신 항목)
        currentHistory.add(0, trimmedQuery)

        // 최대 개수 제한
        if (currentHistory.size > maxSearchHistory) {
            searchHistoryList = currentHistory.take(maxSearchHistory)
        } else {
            searchHistoryList = currentHistory
        }
    }

    fun removeSearchHistory(query: String) {
        val currentHistory = searchHistoryList.toMutableList()
        currentHistory.remove(query.trim())
        searchHistoryList = currentHistory
    }

    fun clearSearchHistory() {
        searchHistoryList = emptyList()
    }

    fun logout() {
        email = ""
        profileImageUrl = ""
    }
}

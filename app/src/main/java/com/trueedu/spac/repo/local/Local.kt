package com.trueedu.spac.repo.local

import android.content.SharedPreferences
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

val LocalMelLocal = staticCompositionLocalOf<Local> {
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

    var deviceId by preferences.string(UUID.randomUUID().toString()) // 고유 id
        private set

    var notificationToken by preferences.string("")

    // user
    var email by preferences.string("")
    var profileImageUrl by preferences.string("")

    // 확인한 notice 마지막 id
    var appNoticeId by preferences.int(0)

    // 종목 다운로드 시각 yyyyMMddHHmm
    var stockUpdatedAt by preferences.long(0L)

    // 스팩 설정
    var spacAnnualProfit by preferences.boolean(false) // 청산 가치 1년 환산 표시

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

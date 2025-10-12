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

    // 종목 다운로드 시각 yyyyMMddHHmm
    var stockUpdatedAt by preferences.long(0L)

    // 스팩 설정
    var spacAnnualProfit by preferences.boolean(false) // 청산 가치 1년 환산 표시

    fun logout() {
        email = ""
        profileImageUrl = ""
    }
}

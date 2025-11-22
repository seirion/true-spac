package com.trueedu.spac.data.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.api.model.dto.firebase.UserRemoteConfig
import com.trueedu.spac.repo.firebase.FirebaseRealtimeDatabase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

val LocalRemoteConfig = staticCompositionLocalOf<RemoteConfig> {
    error("No Local provided")
}

@Singleton
class RemoteConfig @Inject constructor(
    private val userCycle: UserCycle,
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase,
    private val local: com.trueedu.spac.repo.local.Local
) {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logD("RemoteConfig error: ${throwable.message}")
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    // 로그인 이벤트 수집 Job
    private var loginEventJob: Job? = null

    // 초기화 완료 상태를 나타내는 StateFlow
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    // UserRemoteConfig 객체 인스턴스로 관리
    private var config by mutableStateOf(
        UserRemoteConfig(
            adVisible = true,
            refundPriceVisible = false,
            pushToken = null,
            notificationEnabled = false
        )
    )

    // 개별 속성 접근을 위한 getter
    val adVisible: Boolean
        get() = config.adVisible ?: true

    val refundPriceVisible: Boolean
        get() = config.refundPriceVisible ?: false

    val pushToken: String?
        get() = config.pushToken

    val notificationEnabled: Boolean
        get() = config.notificationEnabled

    init {
        loginEventJob = scope.launch {
            userCycle.loginEvent
                .collect { isLogin ->
                    when (isLogin) {
                        true -> {
                            loadConfig()
                            // 로그인 성공 시 저장된 FCM 토큰을 서버에 업로드
                            syncPushTokenAfterLogin()
                        }
                        false -> resetConfig()
                        null -> {
                            // 초기 상태: 아직 로그인 여부가 결정되지 않음
                            logD("RemoteConfig: 로그인 상태 대기 중")
                        }
                    }
                }
        }
    }

    private suspend fun loadConfig() {
        try {
            val loadedConfig = firebaseRealtimeDatabase.loadUserConfig()
            logD("config loaded: $loadedConfig")
            config = loadedConfig
        } catch (e: Exception) {
            logD("Failed to load config", e)
            // 기본값 유지
        } finally {
            // 성공 여부와 관계없이 초기화 완료 표시
            _isInitialized.value = true
            logD("RemoteConfig 초기화 완료")
        }
    }

    private suspend fun resetConfig() {
        config = UserRemoteConfig(
            adVisible = true,
            refundPriceVisible = false,
            pushToken = null,
            notificationEnabled = false
        )
        _isInitialized.value = true
        logD("RemoteConfig 리셋 완료")
    }

    /**
     * 로그인 후 로컬에 저장된 FCM 토큰을 서버에 동기화합니다.
     */
    private suspend fun syncPushTokenAfterLogin() {
        withContext(Dispatchers.IO) {
            try {
                val savedToken = local.notificationToken
                if (savedToken.isNotEmpty()) {
                    logD("🔄 로그인 후 FCM 토큰 서버 동기화 시작: $savedToken")
                    updatePushToken(savedToken)
                    logD("✅ 로그인 후 FCM 토큰 서버 동기화 완료")
                } else {
                    logD("ℹ️ 저장된 FCM 토큰 없음 - 동기화 생략")
                }
            } catch (e: Exception) {
                logD("⚠️ 로그인 후 FCM 토큰 동기화 실패: ${e.message}")
            }
        }
    }

    fun updateAdVisible(visible: Boolean) {
        if (adVisible != visible) {
            updateConfig(config.copy(adVisible = visible))
        }
    }

    fun updateRefundPriceVisible(visible: Boolean) {
        if (refundPriceVisible != visible) {
            updateConfig(config.copy(refundPriceVisible = visible))
        }
    }

    fun updatePushToken(token: String?) {
        if (pushToken != token) {
            updateConfig(config.copy(pushToken = token))
        }
    }

    fun updateNotificationEnabled(enabled: Boolean) {
        if (notificationEnabled != enabled) {
            updateConfig(config.copy(notificationEnabled = enabled))
        }
    }

    private fun updateConfig(newConfig: UserRemoteConfig) {
        val previousConfig = config
        config = newConfig
        scope.launch {
            try {
                firebaseRealtimeDatabase.writeUserConfig(newConfig)
                logD("config saved: $newConfig")
            } catch (e: Exception) {
                logD("Failed to save config", e)
                // 실패 시 상태 복원
                config = previousConfig
            }
        }
    }

    fun onCleared() {
        loginEventJob?.cancel()
        scope.cancel()
    }
}

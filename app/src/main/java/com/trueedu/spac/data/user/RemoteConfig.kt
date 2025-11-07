package com.trueedu.spac.data.user

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.model.UserRemoteConfig
import com.trueedu.spac.repo.firebase.FirebaseRealtimeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

val LocalRemoteConfig = staticCompositionLocalOf<RemoteConfig> {
    error("No Local provided")
}

@Singleton
class RemoteConfig @Inject constructor(
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val adVisible = mutableStateOf(false)

    init {
        scope.launch {
            try {
                val config = firebaseRealtimeDatabase.loadUserConfig()
                adVisible.value = config.adVisible
            } catch (e: Exception) {
                logD("Failed to load config", e)
                // 기본값 유지
            }
        }
    }

    fun setAdVisible(visible: Boolean) {
        if (adVisible.value != visible) {
            val previousValue = adVisible.value
            adVisible.value = visible
            scope.launch {
                try {
                    val config = UserRemoteConfig(adVisible = visible)
                    firebaseRealtimeDatabase.writeUserConfig(config)
                } catch (e: Exception) {
                    logD("Failed to save config", e)
                    // 실패 시 상태 복원
                    adVisible.value = previousValue
                }
            }
        }
    }

    fun onCleared() {
        scope.cancel()
    }
}

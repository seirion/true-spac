package com.trueedu.spac.data.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.api.model.dto.firebase.UserRemoteConfig
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

    var adVisible by mutableStateOf(false)
    var refundPriceVisible by mutableStateOf(false)

    init {
        scope.launch {
            try {
                val config = firebaseRealtimeDatabase.loadUserConfig()
                logD("config: $config")
                adVisible = config.adVisible ?: true
                refundPriceVisible = config.refundPriceVisible ?: false
            } catch (e: Exception) {
                logD("Failed to load config", e)
                // 기본값 유지
            }
        }
    }

    fun updateAdVisible(visible: Boolean) {
        if (adVisible != visible) {
            val previousValue = adVisible
            adVisible = visible
            scope.launch {
                try {
                    val config = UserRemoteConfig(adVisible = visible)
                    firebaseRealtimeDatabase.writeUserConfig(config)
                } catch (e: Exception) {
                    logD("Failed to save config", e)
                    // 실패 시 상태 복원
                    adVisible = previousValue
                }
            }
        }
    }

    fun onCleared() {
        scope.cancel()
    }
}

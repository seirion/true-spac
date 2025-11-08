package com.trueedu.spac

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.AppNotice
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.user.TokenKeyManager
import com.trueedu.spac.repo.firebase.FirebaseRealtimeDatabase
import com.trueedu.spac.util.VersionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseRealtimeDatabase,
    private val trueAnalytics: TrueAnalytics,
    private val tokenKeyManager: TokenKeyManager,
) : ViewModel() {
    private val _forceUpdateVisible = MutableStateFlow(false)
    val forceUpdateVisible: StateFlow<Boolean> = _forceUpdateVisible.asStateFlow()

    private val _appNotice = MutableStateFlow(AppNotice())
    val appNotice: StateFlow<AppNotice> = _appNotice.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val appConfig = firebaseDatabase.getAppConfig()
                logD("Fetched AppConfig from Firebase: $appConfig")

                _appNotice.value = appConfig.notice ?: AppNotice()
                checkForceUpdate(appConfig.minVersion)
            } catch (e: Exception) {
                logD("Failed to fetch AppConfig: ${e.message}")
            }
        }
    }

    private fun checkForceUpdate(minVersion: String?) {
        if (minVersion.isNullOrEmpty()) return

        val currentVersion = BuildConfig.VERSION_NAME
        if (VersionUtils.compareVersions(currentVersion, minVersion) < 0) {
            trueAnalytics.log(
                "force_update__need",
                mapOf("version" to BuildConfig.VERSION_NAME)
            )
            logD("need app update")
            _forceUpdateVisible.value = true
        }
    }

    fun dismissNotice() {
        _appNotice.value = AppNotice()
    }
}

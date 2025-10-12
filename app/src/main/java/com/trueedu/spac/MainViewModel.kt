package com.trueedu.spac

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.AppNotice
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.repo.firebase.FirebaseRealtimeDatabase
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
) : ViewModel() {
    private val _forceUpdateVisible = MutableStateFlow(false)
    val forceUpdateVisible: StateFlow<Boolean> = _forceUpdateVisible.asStateFlow()

    private val _appNotice = MutableStateFlow(AppNotice())
    val appNotice: StateFlow<AppNotice> = _appNotice.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                try {
                    if (firebaseDatabase.needForceUpdate()) {
                        trueAnalytics.log(
                            "force_update__need",
                            mapOf("version" to BuildConfig.VERSION_NAME)
                        )
                        logD("need app update")
                        _forceUpdateVisible.value = true
                    }
                } catch (e: Exception) {
                    logD("Failed to check force update: ${e.message}")
                }
            }

            launch {
                try {
                    firebaseDatabase.appNotice().let {
                        logD("notice: $it")
                        _appNotice.value = it
                    }
                } catch (e: Exception) {
                    logD("Failed to fetch app notice: ${e.message}")
                }
            }
        }
    }

    fun dismissNotice() {
        _appNotice.value = AppNotice()
    }
}

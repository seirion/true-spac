package com.trueedu.spac.ui.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.data.user.GoogleAuthClient
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.repo.firebase.FirebaseRealtimeDatabase
import com.trueedu.spac.repo.local.Local
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userCycle: UserCycle,
    private val local: Local,
    private val trueAnalytics: TrueAnalytics,
    private val firebaseRealtimeDatabase: FirebaseRealtimeDatabase,
    private val googleAuthClient: GoogleAuthClient,
) : ViewModel() {
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    fun email() = userCycle.email.value
    fun profileImageUrl() = userCycle.profileImageUrl.value

    // Worker 상태 조회 메서드
    fun isAdminMode(): Boolean = local.getUserKey().isValid()

    fun loggedIn(): Boolean = userCycle.loggedIn()

    fun deleteAccount(
        onSuccess: () -> Unit,
        onFail: () -> Unit,
    ) {
        viewModelScope.launch {
            firebaseRealtimeDatabase.deleteUser(
                onSuccess = {
                    viewModelScope.launch {
                        googleAuthClient.deleteAccount()
                            .onSuccess {
                                onSuccess()
                            }
                            .onFailure { exception ->
                                logE("Firebase Authentication 계정 삭제 실패: ${exception.message}")
                                trueAnalytics.log("delete_account_fail")
                                onFail()
                            }
                    }
                },
                onFail = onFail
            )
        }
    }
}

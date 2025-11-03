package com.trueedu.spac.ui.profile

import android.content.Context
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
class MoreViewModel @Inject constructor(
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

    fun logout(
        onSuccess: () -> Unit = {},
        onFail: () -> Unit = {},
    ) {
        viewModelScope.launch {
            try {
                googleAuthClient.signOut()
                trueAnalytics.log("logout")
                onSuccess()
            } catch (e: Exception) {
                logE("로그아웃 실패", e)
                trueAnalytics.log("logout_fail", mapOf("error" to (e.message ?: "unknown")))
                onFail()
            }
        }
    }

    fun deleteAccount(
        context: Context,
        onSuccess: () -> Unit,
        onFail: () -> Unit,
    ) {
        if (_loading.value) {
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                // 1. Firebase Realtime Database 데이터 삭제 (인증된 상태에서)
                firebaseRealtimeDatabase.deleteUser(
                    onSuccess = {
                        // 2. Firebase Authentication 계정 삭제 (재인증 포함)
                        viewModelScope.launch {
                            googleAuthClient.deleteAccount(context)
                                .onSuccess {
                                    _loading.value = false
                                    trueAnalytics.log("delete_account_success")
                                    onSuccess()
                                }
                                .onFailure { exception ->
                                    _loading.value = false
                                    logE("Firebase Authentication 계정 삭제 실패: ${exception.message}")
                                    trueAnalytics.log("delete_account_fail")
                                    onFail()
                                }
                        }
                    },
                    onFail = {
                        _loading.value = false
                        logE("Firebase Realtime Database 데이터 삭제 실패")
                        trueAnalytics.log("delete_account_fail")
                        onFail()
                    }
                )
            } catch (e: Exception) {
                _loading.value = false
                logE("계정 삭제 중 예외 발생", e)
                trueAnalytics.log("delete_account_fail")
                onFail()
            }
        }
    }
}


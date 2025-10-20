package com.trueedu.spac.ui.profile

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.repo.local.Local
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userCycle: UserCycle,
    private val local: Local,
) : ViewModel() {
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    fun email() = userCycle.email.value
    fun profileImageUrl() = userCycle.profileImageUrl.value

    // Worker 상태 조회 메서드
    fun isAdminMode(): Boolean = local.getUserKey().isValid()
}

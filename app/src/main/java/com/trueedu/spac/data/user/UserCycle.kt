package com.trueedu.spac.data.user

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.repo.local.Local
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

val LocalUserCycle = staticCompositionLocalOf<UserCycle> {
    error("No UserCycle provided")
}

@Singleton
class UserCycle @Inject constructor(
    private val local: Local,
    private val trueAnalytics: TrueAnalytics,
) {
    private val _loginEvent = MutableStateFlow<Boolean?>(null)
    val loginEvent: StateFlow<Boolean?> = _loginEvent.asStateFlow()

    private val _email = mutableStateOf(local.email)
    val email: State<String> = _email
    private val _profileImageUrl = mutableStateOf(local.profileImageUrl)
    val profileImageUrl: State<String> = _profileImageUrl

    fun login(
        email: String,
        profileImageUrl: String,
    ) {
        local.email = email
        local.profileImageUrl = profileImageUrl
        this._email.value = email
        this._profileImageUrl.value = profileImageUrl

        _loginEvent.value = true
        trueAnalytics.log("login")
    }

    fun silentLogin() {
        this._email.value = local.email
        this._profileImageUrl.value = local.profileImageUrl

        _loginEvent.value = true
        trueAnalytics.log("silent_login")
        trueAnalytics.setUserId(local.email)
    }

    fun logout() {
        _email.value = ""
        _profileImageUrl.value = ""
        local.logout()
        _loginEvent.value = false
        trueAnalytics.log("logout")
    }

    fun loggedIn() = _email.value.isNotEmpty()
}

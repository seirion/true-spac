package com.trueedu.spac.data.config

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

val LocalFeatureConfig = staticCompositionLocalOf<LocalFeature> {
    error("No LocalFeature provided")
}

@Singleton
class LocalFeature @Inject constructor(
    private val preferences: SharedPreferences
) {
    /**
     * 인트로 화면을 항상 표시할지 여부
     * 디버깅/테스트 용도로 사용
     *
     * @default false
     */
    val forceShowIntro = createPreferenceState("force_show_intro", false)

    private fun createPreferenceState(key: String, defaultValue: Boolean): MutableState<Boolean> {
        val state = mutableStateOf(preferences.getBoolean(key, defaultValue))

        return object : MutableState<Boolean> {
            override var value: Boolean
                get() = state.value
                set(value) {
                    state.value = value
                    preferences.edit { putBoolean(key, value) }
                }

            override fun component1(): Boolean = state.value
            override fun component2(): (Boolean) -> Unit = { value = it }
        }
    }
}

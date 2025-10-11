package com.trueedu.spac.analytics

import android.app.Application
import androidx.compose.runtime.staticCompositionLocalOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

val LocalTrueAnalytics = staticCompositionLocalOf<TrueAnalytics> {
    error("No TrueAnalytics provided")
}

@Singleton
class TrueAnalytics @Inject constructor(@ApplicationContext application: Application) {
    private val logger: List<BaseAnalytics> = listOf(
        AmplitudeAnalytics(),
    )

    init {
        logger.forEach { it.init(application) }
    }

    fun setUserId(userId: String) {
        logger.forEach { it.setUserId(userId) }
    }

    fun setUserProperties(properties: Map<String, Any>) {
        logger.forEach { it.setUserProperties(properties) }
    }

    fun clickButton(buttonName: String) {
        log(buttonName)
    }

    fun clickButton(buttonName: String, params: Map<String, Any>) {
        log(buttonName, params)
    }

    /**
     * "prev" 값은 토글 버큰 누르기 전 상태임
     */
    private fun toggleButtonState(prevState: Boolean) = if (prevState) "on" else "off"

    /**
     * "result" 값은 토클 버튼 누른 후의 상태임
     */
    private fun toggleButtonResult(prevState: Boolean) = toggleButtonState(!prevState)

    fun clickToggleButton(buttonName: String, prevState: Boolean) {
        log(buttonName, mapOf("prev" to toggleButtonState(prevState), "result" to toggleButtonResult(prevState)))
    }

    fun clickToggleButton(buttonName: String, prevState: Boolean, params: Map<String, Any> = emptyMap()) {
        val paramsAll = params + mapOf("prev" to toggleButtonState(prevState), "result" to toggleButtonResult(prevState))
        log(buttonName, paramsAll)
    }

    fun log(event: String, params: Map<String, Any> = emptyMap()) {
        logAll(event, params)
    }

    /**
     * 개발/디버깅 용 로그
     */
    fun logDev(event: String, params: Map<String, Any> = emptyMap()) {
        logAll(event, params + mapOf("dev" to true))
    }

    private fun logAll(event: String, params: Map<String, Any>) {
        logger.forEach { it.log(event, params) }
    }

    /**
     * 특정 화면 진입
     */
    fun enterView(event: String, params: Map<String, Any> = emptyMap()) {
        log(event, params)
    }

    fun shutdown() {
        logger.forEach { it.shutdown() }
    }
}

fun onOffString(on: Boolean) = if (on) "on" else "off"
fun yesNoString(yes: Boolean) = if (yes) "yes" else "no"

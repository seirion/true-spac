package com.trueedu.spac.analytics

import android.app.Application

interface BaseAnalytics {
    fun init(application: Application) {}
    fun log(event: String, params: Map<String, Any>)
    fun setUserId(userId: String)
    fun setUserProperties(properties: Map<String, Any>)
    fun shutdown() {}
}

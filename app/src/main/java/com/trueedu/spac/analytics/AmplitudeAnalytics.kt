package com.trueedu.spac.analytics

import android.app.Application
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.trueedu.spac.R
import org.json.JSONObject

class AmplitudeAnalytics : BaseAnalytics {
    private lateinit var analytics: AmplitudeClient

    override fun init(application: Application) {
        val apiKey = application.getString(R.string.amplitude_api_key)
        analytics = Amplitude.getInstance()
            .initialize(application.applicationContext, apiKey)
            .enableForegroundTracking(application)
        analytics.trackSessionEvents(true)
    }

    override fun log(event: String, params: Map<String, Any>) {
        analytics.logEvent(event, JSONObject(params))
    }

    override fun setUserId(userId: String) {
        analytics.userId = userId
    }

    override fun setUserProperties(properties: Map<String, Any>) {
        analytics.setUserProperties(JSONObject(properties))
    }
}

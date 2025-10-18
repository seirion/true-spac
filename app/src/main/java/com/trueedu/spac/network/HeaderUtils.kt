package com.trueedu.spac.network

import okhttp3.Headers

fun getApiHeaders(
    appKey: String,
    apSecret: String,
    accessToken: String,
): Headers {
    val headers = Headers.Builder()

    listOf(
        "content-type" to "application/json",
        "appkey" to appKey,
        "appsecret" to apSecret,
        "authorization" to "Bearer $accessToken",
    ).forEach { (key, value) ->
        headers[key] = value
    }

    return headers.build()
}


package com.trueedu.spac.network

import com.trueedu.spac.repo.local.Local
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val local: Local,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val userKey = local.getUserKey()
        val appKey = userKey.appKey ?: ""
        val appSecret = userKey.appSecret ?: ""
        val accessToken = local.accessToken

        val headers0 = chain.request().headers
        val headers1 = getApiHeaders(appKey, appSecret, accessToken)

        val headers = headers0.newBuilder()
            .addAll(headers1)
            .build()

        val request = chain.request().newBuilder()
            .headers(headers)
            .build()

        return chain.proceed(request)
    }
}

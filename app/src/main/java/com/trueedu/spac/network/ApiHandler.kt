package com.trueedu.spac.network

import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response

@Throws(HttpException::class)
internal suspend fun <T> handleApi(call: suspend () -> Response<T>): T {
    val response = call()
    if (response.isSuccessful) {
        return response.body() ?: throw IllegalStateException("Response body is null")
    } else {
        throw HttpException(response)
    }
}

internal fun <T> apiCallFlow(call: suspend () -> Response<T>) = flow {
    emit(handleApi(call))
}

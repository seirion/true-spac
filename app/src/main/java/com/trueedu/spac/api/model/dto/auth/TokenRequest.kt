package com.trueedu.spac.api.model.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class TokenRequest(
    @SerialName(value = "grant_type")
    val grantType: String,
    @SerialName(value = "appkey")
    val appKey: String?,
    @SerialName(value = "appsecret")
    val appSecret: String?,
)

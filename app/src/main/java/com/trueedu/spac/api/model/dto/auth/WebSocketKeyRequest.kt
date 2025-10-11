package com.trueedu.spac.api.model.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * TokenRequest 와 데이터가 같지만
 * 어의 없게도 appsecret 대신 secretkey 로 보내야 함
 */
@Serializable
data class WebSocketKeyRequest(
    @SerialName(value = "grant_type")
    val grantType: String,
    @SerialName(value = "appkey")
    val appKey: String,
    @SerialName(value = "secretkey")
    val secretKey: String,
)
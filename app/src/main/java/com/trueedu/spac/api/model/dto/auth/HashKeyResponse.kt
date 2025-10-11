package com.trueedu.spac.api.model.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HashKeyResponse(
    @SerialName(value = "BODY")
    val body: HashKeyBody,
    @SerialName(value = "HASH")
    val hash: String,
)

@Serializable
data class HashKeyBody(
    @SerialName(value = "JsonBody")
    val jsonBody: String,
)

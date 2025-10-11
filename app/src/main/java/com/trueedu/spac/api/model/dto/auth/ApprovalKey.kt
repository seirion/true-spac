package com.trueedu.spac.api.model.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApprovalKeyResponse(
    @SerialName("approval_key")
    val approvalKey: String,
)

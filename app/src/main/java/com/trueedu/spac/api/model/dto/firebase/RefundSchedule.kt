package com.trueedu.spac.api.model.dto.firebase

import kotlinx.serialization.Serializable

@Serializable
data class RefundSchedule(
    val nameKr: String = "",
    val code: String = "",
    val date: String = "", // yyyyMMdd
    val refundAmount: Double? = null,
    val fixed: Boolean = false,
)

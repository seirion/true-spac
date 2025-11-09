package com.trueedu.spac.api.model.dto.firebase

data class RefundSchedule(
    val nameKr: String = "",
    val code: String = "",
    val date: String = "", // yyyyMMdd
    val refundAmount: Double? = null
)

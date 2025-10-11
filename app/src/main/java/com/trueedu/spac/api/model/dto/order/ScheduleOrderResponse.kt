package com.trueedu.spac.api.model.dto.order

import com.trueedu.spac.api.model.dto.common.ApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleOrderResponse(
    @SerialName("rt_cd")
    override val rtCd: String,
    @SerialName("msg_cd")
    override val msgCd: String,
    @SerialName("msg1")
    override val msg1: String?,
    val output: ScheduleOrderSeq?,
) : ApiResponse

@Serializable
data class ScheduleOrderSeq(
    @SerialName("RSVN_ORD_SEQ")
    val rsvnOrdSeq: String, // 예약주문번호
)

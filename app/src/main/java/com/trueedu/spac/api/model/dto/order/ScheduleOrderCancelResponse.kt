package com.trueedu.spac.api.model.dto.order

import com.trueedu.spac.api.model.dto.common.ApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleOrderCancelResponse(
    @SerialName("rt_cd")
    override val rtCd: String,
    @SerialName("msg_cd")
    override val msgCd: String,
    override val msg1: String?,
    val msg: String?,
    val output: ResponseResult?,
) : ApiResponse

@Serializable
data class ResponseResult(
    // 문서에는 소문자로 되어 있지만, 실제로 대문자로 오고 있음
    @SerialName("NRML_PRCS_YN")
    val result: String, // Y or N
)

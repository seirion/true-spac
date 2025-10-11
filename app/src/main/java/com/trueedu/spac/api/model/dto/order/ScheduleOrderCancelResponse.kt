package com.trueedu.spac.api.model.dto.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleOrderCancelResponse(
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드 - "KIOK0560"
    val msg: String?, // 응답메세지
    val msg1: String?, // 응답메세지 - 문서와는 달리 msg1 필드로 성공 또는 실패 메시지가 오고 있음
    val output: ResponseResult?,
)

@Serializable
data class ResponseResult(
    // 문서에는 소문자로 되어 있지만, 실제로 대문자로 오고 있음
    @SerialName("NRML_PRCS_YN")
    val result: String, // Y or N
)

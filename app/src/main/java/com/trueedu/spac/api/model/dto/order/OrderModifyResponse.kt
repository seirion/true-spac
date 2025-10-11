package com.trueedu.spac.api.model.dto.order

import com.trueedu.spac.api.model.dto.common.ApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderModifyResponse(
    @SerialName("output")
    val orderModifyDetail: OrderModifyDetail?,
    @SerialName("rt_cd")
    override val rtCd: String,
    @SerialName("msg_cd")
    override val msgCd: String,
    override val msg1: String?,
    val msg: String?,
) : ApiResponse

@Serializable
data class OrderModifyDetail(
    @SerialName("ODNO")
    val orderNo: String, // 정정 주문시 한국투자증권 시스템에서 채번된 주문번호
    @SerialName("ORD_TMD")
    val orderTime: String, // 주문시각 (HHmmss)
)

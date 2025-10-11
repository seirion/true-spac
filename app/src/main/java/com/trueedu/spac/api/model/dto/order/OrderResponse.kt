package com.trueedu.spac.api.model.dto.order

import com.trueedu.spac.api.model.dto.common.ApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    @SerialName("output")
    val orderDetail: OrderDetail?,
    @SerialName("rt_cd")
    override val rtCd: String,
    @SerialName("msg_cd")
    override val msgCd: String,
    override val msg1: String?,
    val msg: String?,
) : ApiResponse
@Serializable
data class OrderDetail(
    @SerialName("KRX_FWDG_ORD_ORGNO")
    val krxForwardingOrderOrgNumber: String, // 한국거래소전송주문조직번호
    @SerialName("ODNO")
    val orderNumber: String, // 주문번호
    @SerialName("ORD_TMD")
    val orderTime: String, // 주문시각(HHmmss)
)

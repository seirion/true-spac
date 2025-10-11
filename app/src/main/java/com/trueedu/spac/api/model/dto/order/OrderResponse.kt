package com.trueedu.spac.api.model.dto.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    @SerialName("output")
    val orderDetail: OrderDetail?,
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드 - "MCA00000"
    val msg: String?, // 응답메세지 - "정상처리 되었습니다."
    val msg1: String?,
)
@Serializable
data class OrderDetail(
    @SerialName("KRX_FWDG_ORD_ORGNO")
    val krxForwardingOrderOrgNumber: String, // 한국거래소전송주문조직번호
    @SerialName("ODNO")
    val orderNumber: String, // 주문번호
    @SerialName("ORD_TMD")
    val orderTime: String, // 주문시각(HHmmss)
)

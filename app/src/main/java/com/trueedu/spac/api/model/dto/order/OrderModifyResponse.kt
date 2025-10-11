package com.trueedu.spac.api.model.dto.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderModifyResponse(
    @SerialName("output")
    val orderModifyDetail: OrderModifyDetail?,
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드 - "MCA00000"
    val msg: String?, // 응답메세지 - "정상처리 되었습니다."
    val msg1: String?,
)

@Serializable
data class OrderModifyDetail(
    // -KRX_FWDG_ORD_ORGNO	한국거래소전송주문조직번호	String	Y	5	주문시 한국투자증권 시스템에서 지정된 영업점코드
    @SerialName("ODNO")
    val orderNo: String, // 정정 주문시 한국투자증권 시스템에서 채번된 주문번호
    @SerialName("ORD_TMD")
    val orderTime: String, // 주문시각(시분초HHMMSS)
)

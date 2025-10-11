package com.trueedu.spac.api.model.dto.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleOrderResult(
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드 - "KIOK0560"
    @SerialName("msg1")
    val msg: String, // 응답메세지
    @SerialName("output")
    val list: List<ScheduleOrderResultDetail>?,
    @SerialName("ctx_area_fk200")
    val fk200: String,
    @SerialName("ctx_area_nk200")
    val nk200: String,
)

@Serializable
data class ScheduleOrderResultDetail(
    @SerialName("rsvn_ord_seq")
    val seq: String, // 예약주문 순번
    @SerialName("rsvn_ord_ord_dt")
    val orderDate: String, // 예약주문주문일자
    @SerialName("rsvn_ord_rcit_dt")
    val receivedDate: String, // 예약주문접수일자
    @SerialName("pdno")
    val code: String, // 상품번호
    @SerialName("ord_dvsn_cd")
    val orderDivisionCode: String, // 주문구분코드
    @SerialName("ord_rsvn_qty")
    val orderReservedQuantity: String, // 주문예약수량
    @SerialName("tot_ccld_qty")
    val totalClearedQuantity: String, // 총체결수량
    @SerialName("cncl_ord_dt")
    val cancelOrderDate: String, // 취소주문일자
    @SerialName("ord_tmd")
    val orderTime: String, // 주문시각
    // -ctac_tlno	연락전화번호	String	N	20

    @SerialName("rjct_rson2")
    val rejectReason: String, // 거부사유2
    @SerialName("odno")
    val orderNumber: String, // 주문번호
    @SerialName("rsvn_ord_rcit_tmd")
    val receivedTime: String, // 예약주문접수시각
    @SerialName("kor_item_shtn_name")
    val nameKr: String, // 한글종목단축명
    @SerialName("sll_buy_dvsn_cd")
    val sellBuyDivisionCode: String, // 매도매수구분코드
    @SerialName("ord_rsvn_unpr")
    val price: String, // 주문예약단가
    @SerialName("tot_ccld_amt")
    val totalClearedAmount: String, // 총체결금액
    // @SerialName("loan_dt")
    // loan_dt	대출일자	String	N	8
    @SerialName("cncl_rcit_tmd")
    val cancelReceivedTime: String, // 취소접수시각
    @SerialName("prcs_rslt")
    val processResult: String, // 처리결과
    @SerialName("ord_dvsn_name")
    val orderDivisionName: String, // 주문구분명
    // @SerialName("tmnl_mdia_kind_cd")
    // val terminalMediaKindCode: String, // 단말매체종류코드
    @SerialName("rsvn_end_dt")
    val endDate: String, // 예약종료일자
) {
    // '처리' 상태인 예약은 삭제, 수정 불가
    fun disabled() = processResult == "처리"
}

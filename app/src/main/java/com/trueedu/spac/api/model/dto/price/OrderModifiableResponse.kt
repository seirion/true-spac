package com.trueedu.spac.api.model.dto.price

import com.trueedu.spac.api.model.dto.common.ApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderModifiableResponse(
    @SerialName("output")
    val orderModifiableDetail: List<OrderModifiableDetail>,
    @SerialName("rt_cd")
    override val rtCd: String,
    @SerialName("msg_cd")
    override val msgCd: String,
    override val msg1: String,

    // 다음 연속 조회시 사용
    @SerialName("ctx_area_fk100")
    val fk100: String, // 연속조회검색조건100
    @SerialName("ctx_area_nk100")
    val nk100: String, // 연속조회키100
) : ApiResponse

@Serializable
data class OrderModifiableDetail(
    @SerialName("ord_gno_brno")
    val ordGnoBrno: String, // 주문채번지점번호 - 주문시 한국투자증권 시스템에서 지정된 영업점코드
    @SerialName("odno")
    val orderNo: String, // 주문번호 - 주문시 한국투자증권 시스템에서 채번된 주문번호
    @SerialName("orgn_odno")
    val originalOrderNo: String, // 원주문번호 - 정정/취소주문 인경우 원주문번호
    @SerialName("ord_dvsn_name")
    val orderDivisionName: String, // 주문구분명 - 주문구분명
    @SerialName("pdno")
    val code: String, // 종목번호(뒤 6자리만 해당)
    @SerialName("prdt_name")
    val nameKr: String, // 상품명 - 종목명
    @SerialName("rvse_cncl_dvsn_name")
    val revisionCancellationDivisionName : String, // 정정취소구분명 - 정정 또는 취소 여부 표시
    @SerialName("ord_qty")
    val quantity: String, // 주문수량
    @SerialName("ord_unpr")
    val price: String, // 주문단가
    @SerialName("ord_tmd")
    val orderTime: String, //주문시각(시분초HHMMSS)
    @SerialName("tot_ccld_qty")
    val totalContractedQuantity: String, // 총체결수량 - 주문 수량 중 체결된 수량
    @SerialName("tot_ccld_amt")
    val totalContractedAmount: String, // 총체결금액 -	주문금액 중 체결금액
    @SerialName("psbl_qty")
    val possibleQuantity: String, // 가능수량 - 정정/취소 주문 가능 수량
    @SerialName("sll_buy_dvsn_cd")
    val sellBuyDivisionCode: String, // 매도매수구분코드 - 01 : 매도 02 : 매수
    /*
    00 : 지정가
    01 : 시장가
    02 : 조건부지정가
    03 : 최유리지정가
    04 : 최우선지정가
    05 : 장전 시간외
    06 : 장후 시간외
    07 : 시간외 단일가
    08 : 자기주식
    09 : 자기주식S-Option
    10 : 자기주식금전신탁
    11 : IOC지정가 (즉시체결,잔량취소)
    12 : FOK지정가 (즉시체결,전량취소)
    13 : IOC시장가 (즉시체결,잔량취소)
    14 : FOK시장가 (즉시체결,전량취소)
    15 : IOC최유리 (즉시체결,잔량취소)
    16 : FOK최유리 (즉시체결,전량취소)
    51 : 장중대량
    */
    @SerialName("ord_dvsn_cd")
    val orderDivisionCode: String, // 주문구분코드 - 주문구분코드

// -mgco_aptm_odno	운용사지정주문번호	String	Y	12	주문 번호 (운용사 통한 주문)
)

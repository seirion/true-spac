package com.trueedu.spac.api.model.dto.price

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderExecutionResponse(
    @SerialName("output1")
    val orderExecutionDetail: List<OrderExecutionDetail>?,
    @SerialName("output2")
    val orderExecutionSummary: OrderExecutionSummary?,
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드 - "KIOK0560"
    // 응답메세지 - "정상처리 되었습니다."
    val msg: String?,
    val msg1: String?,

    // 다음 연속 조회시 사용
    @SerialName("ctx_area_fk100")
    val fk100: String, // 연속조회검색조건100
    @SerialName("ctx_area_nk100")
    val nk100: String, // 연속조회키100
)

@Serializable
data class OrderExecutionDetail(
    @SerialName("ord_dt")
    val orderDate: String, // 주문일자
    @SerialName("ord_gno_brno")
    val orderBranchNo: String, // 주문채번지점번호 - 주문시 한국투자증권 시스템에서 지정된 영업점코드
    @SerialName("odno")
    val orderNo: String, // 주문번호 - 주문시 한국투자증권 시스템에서 채번된 주문번호, 지점별 일자별로 채번됨
             // * 주문번호 유일조건: ord_dt(주문일자) + ord_gno_brno(주문채번지점번호) + odno(주문번호)
    @SerialName("orgn_odno")
    val originalOrderNo: String, // 원주문번호 - 이전 주문에 채번된 주문번호
    @SerialName("ord_dvsn_name")
    val orderDivisionName: String, // 주문구분명
    @SerialName("sll_buy_dvsn_cd")
    val sellBuyDivisionCode: String, // 매도매수구분코드 - 01 : 매도 02 : 매수
    @SerialName("sll_buy_dvsn_cd_name")
    val sellBuyCodeName: String, // 매도매수구분명 - 반대매매 인경우 "임의매도"로 표시됨
                        // 정정취소여부가 Y이면 *이 붙음
                        //ex) 매수취소* = 매수취소가 완료됨
    @SerialName("pdno")
    val code: String, // 상품번호
    @SerialName("prdt_name")
    val nameKr: String, // 상품명
    @SerialName("ord_qty")
    val orderQuantity: String, // 주문수량
    @SerialName("ord_unpr")
    val orderUnitPrice: String, // 주문단가
    @SerialName("ord_tmd")
    val orderTime: String, // 주문시각
    @SerialName("tot_ccld_qty")
    val totalConcludedQuantity: String, // 총체결수량
    @SerialName("avg_prvs")
    val averagePrice: String, // 체결평균가 ( 총체결금액 / 총체결수량 )
    @SerialName("cncl_yn")
    val cancellationYn: String, // 취소여부
    @SerialName("tot_ccld_amt")
    val totalConcludedAmount: String, // 총체결금액
    @SerialName("loan_dt")
    val loanDate: String, // 대출일자
    /**
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
     */
    @SerialName("ord_dvsn_cd")
    val orderDivisionCode: String, // 주문구분코드
    @SerialName("cncl_cfrm_qty")
    val cancellationConfirmationQuantity: String, // 취소확인수량
    @SerialName("rmn_qty")
    val remainingQuantity: String, // 잔여수량
    @SerialName("rjct_qty")
    val rejectionQuantity: String, // 거부수량
    @SerialName("ccld_cndt_name")
    val concludedConditionName: String, // 체결조건명
    @SerialName("infm_tmd")
    val informationTime: String, // 통보시각 - 실전투자계좌로는 해당값이 제공되지 않습니다.
    // -ctac_tlno	연락전화번호	String	Y	20
    @SerialName("prdt_type_cd")
    val productTypeCode: String, // 상품유형코드 - 300 : 주식 301 : 선물옵션 302 : 채권 306 : ELS
    @SerialName("excg_dvsn_cd")
    val exchangeDivisionCode: String, // 거래소구분코드 - 01 : 한국증권 02 : 증권거래소 03 : 코스닥 04 : K-OTC
    // 05 : 선물거래소 06 : CME 07 : EUREX
    // 21 : 금현물
    // 51 : 홍콩 52 : 상해B 53 : 심천 54 : 홍콩거래소 55 : 미국 56 : 일본 57 : 상해A
    // 58 : 심천A 59 : 베트남 61 : 장전시간외시장 64 : 경쟁대량매매 65 : 경매매시장
    // 81 : 시간외단일가시장
)

@Serializable
data class OrderExecutionSummary(
    @SerialName("tot_ord_qty")
    val totalOrderQuantity: String, // 총주문수량: 미체결주문수량 + 체결수량 (취소주문제외)
    @SerialName("tot_ccld_qty")
    val totalConcludedQuantity: String, // 총체결수량
    @SerialName("pchs_avg_pric")
    val purchaseAveragePrice: String, // 매입평균가
    @SerialName("tot_ccld_amt")
    val totalConcludedAmount: String, // 총체결금액
    @SerialName("prsm_tlex_smtl") // presumed tax, levy, and settlement total
    val estimatedTaxAndFee: String, // 추정제비용합계 - 해당 값은 당일 데이터에 대해서만 제공됩니다.
)

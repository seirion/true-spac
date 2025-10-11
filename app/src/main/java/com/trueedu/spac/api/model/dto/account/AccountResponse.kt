package com.trueedu.spac.api.model.dto.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountResponse(
    val output1: List<AccountAsset>,
    val output2: List<AccountDetail>,
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드 - "KIOK0510"
    val msg1: String, // 응답메세지 - "정상처리 되었습니다."

    // 다음 연속 조회시 사용
    @SerialName("ctx_area_fk100")
    val fk100: String, // 연속조회검색조건100
    @SerialName("ctx_area_nk100")
    val nk100: String, // 연속조회키100
)

@Serializable
data class AccountAsset(
    @SerialName("pdno")
    val code: String, // 상품번호 (종목코드)
    @SerialName("prdt_name")
    val nameKr: String, // 상품명
    @SerialName("trad_dvsn_name")
    val tradeType: String, // 매매구분명 - "현금"
    @SerialName("bfdy_buy_qty")
    val prevDayBuyQuantity: String, // 전일매수수량
    @SerialName("bfdy_sll_qty")
    val prevDaySellQuantity: String, // 금일매수수량
    @SerialName("thdt_buyqty") // api 문서가 오타인데 실제로 값도 이렇게 옴
    val todayBuyQuantity: String, // 금일매수수량
    @SerialName("thdt_sll_qty")
    val todaySellQuantity: String, // 금일매도수량
    @SerialName("hldg_qty")
    val holdingQuantity: String, // 보유수량
    @SerialName("ord_psbl_qty")
    val orderPossibleQuantity: String, // 주문가능수량
    @SerialName("pchs_avg_pric")
    val purchaseAveragePrice: String, // 매입평균가격
    @SerialName("pchs_amt")
    val purchaseAmount: String, // 매입금액
    @SerialName("prpr")
    val currentPrice: String, // 현재가
    @SerialName("evlu_amt")
    val evaluationAmount: String, // 평가금액
    @SerialName("evlu_pfls_amt")
    val profitLossAmount: String, // 평가손익금액
    @SerialName("evlu_pfls_rt")
    val profitLossRate: String, // 평가손익율
    @SerialName("evlu_erng_rt")
    val evaluationEarningsRate: String, // 평가수익율
    @SerialName("loan_dt")
    val loanDate: String, // 대출일자 - INQR_DVSN(조회구분)을 01(대출일별)로 설정해야 값이 나옴
    @SerialName("loan_amt")
    val loanAmount: String, // 대출금액
    @SerialName("stln_slng_chgs")
    val settlementSellingCharges: String, // 대주매각대금
    @SerialName("expd_dt")
    val expirationDate: String, // 만기일자
    @SerialName("fltt_rt")
    val priceChangeRate: String, // 전일 대비 등락율
    @SerialName("bfdy_cprs_icdc")
    val priceChange: String, // 전일대비증감 (주식 가격)
    @SerialName("item_mgna_rt_name")
    val itemMarginRateName: String, // 종목증거금율명
    @SerialName("grta_rt_name")
    val depositRateName: String, // 보증금율명
    @SerialName("sbst_pric")
    val substitutePrice: String, // 대용가격
    @SerialName("stck_loan_unpr")
    val stockLoanUnitPrice: String, // 주식대출단가
)

@Serializable
data class AccountDetail(
    @SerialName("dnca_tot_amt")
    val depositAccountTotalAmount: String, // 예수금총금액
    @SerialName("nxdy_excc_amt")
    val nextDayExcessAmount: String, // 익일정산금액 - D+1 예수금
    @SerialName("prvs_rcdl_excc_amt")
    val previousRedemptionExcessAmount: String, // 가수도정산금액 - D+2 예수금
    @SerialName("cma_evlu_amt")
    val cmaEvaluationAmount: String, // CMA평가금액
    @SerialName("bfdy_buy_amt")
    val previousDayBuyAmount: String, // 전일매수금액
    @SerialName("thdt_buy_amt")
    val todayBuyAmount: String, // 금일매수금액
    @SerialName("nxdy_auto_rdpt_amt")
    val nextDayAutoRedemptionAmount: String, // 익일자동상환금액
    @SerialName("bfdy_sll_amt")
    val previousDaySellAmount: String, // 전일매도금액
    @SerialName("thdt_sll_amt")
    val todaySellAmount: String, // 금일매도금액
    @SerialName("d2_auto_rdpt_amt")
    val d2AutoRedemptionAmount: String, // D+2자동상환금액
    @SerialName("bfdy_tlex_amt")
    val previousDayTotalExpensesAmount: String, // 전일제비용금액
    @SerialName("tot_loan_amt")
    val totalLoanAmount: String, // 총대출금액
    @SerialName("scts_evlu_amt")
    val stockEvaluationAmount: String, // 유가평가금액
    @SerialName("tot_evlu_amt")
    val totalEvaluationAmount: String, // 총평가금액 - 유가증권 평가금액 합계금액 + D+2 예수금
    @SerialName("nass_amt")
    val netAssetAmount: String, // 순자산금액
    @SerialName("fncg_gld_auto_rdpt_yn")
    val autoRedemptionYn: String, // 융자금자동상환여부 - 보유현금에 대한 융자금만 차감여부
            // 신용융자 매수체결 시점에서는 융자비율을 매매대금 100%로 계산 하였다가
            // 수도결제일에 보증금에 해당하는 금액을 고객의 현금으로 충당하여 융자금을 감소시키는 업무
    @SerialName("pchs_amt_smtl_amt")
    val purchaseAmountSumTotalAmount: String, //매입금액합계금액 (원금)
    @SerialName("evlu_amt_smtl_amt")
    val evaluationAmountSumTotalAmount: String, //평가금액합계금액
    @SerialName("evlu_pfls_smtl_amt")
    val profitLossSumTotalAmount: String, //평가손익금액합계금액
    @SerialName("tot_stln_slng_chgs")
    val totalSettlementSellingCharges: String, //총대주매각대금
    @SerialName("bfdy_tot_asst_evlu_amt")
    val previousTotalAssetEvaluationAmount: String, //전일총자산평가금액
    @SerialName("asst_icdc_amt")
    val assetChangeAmount: String, // 자산증감액 (Asset Increase/Decrease Amount) - 일간 수익
    @SerialName("asst_icdc_erng_rt")
    val assetChangeRate: String, // 자산증감율 (Asset Increase/Decrease Earning Rate) - 데이터 미제공
) {
    // 총 수익률
    fun totalProfitRate(): Double {
        try {
            val cost = purchaseAmountSumTotalAmount.toDouble() // 원금
            val value = evaluationAmountSumTotalAmount.toDouble() // 평가액

            if (cost == 0.0) return 0.0
            return (value - cost) / cost * 100
        } catch (_: NumberFormatException) {
            return 0.0
        }
    }

    // 전날 대비 수익률
    fun dailyProfitRate(): Double {
        try {
            val profit = assetChangeAmount.toDouble()
            // FIXME: 예수금 포함이라서 정확하지 않음
            val cost = previousTotalAssetEvaluationAmount.toDouble() // 전일 자산

            if (cost == 0.0) return 0.0
            return profit / cost * 100
        } catch (_: Exception) {
            return 0.0
        }
    }
}

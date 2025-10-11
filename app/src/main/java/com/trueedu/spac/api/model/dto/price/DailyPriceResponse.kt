package com.trueedu.spac.api.model.dto.price

import com.trueedu.spac.api.model.dto.common.ApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DailyPriceResponse(
    @SerialName("output1")
    val stockDetail: StockDetail?,
    @SerialName("output2")
    val dailyPrices: List<DailyPrice>,
    @SerialName("rt_cd")
    override val rtCd: String,
    @SerialName("msg_cd")
    override val msgCd: String,
    override val msg1: String,
) : ApiResponse

@Serializable
data class StockDetail(
    @SerialName("prdy_vrss")
    val priceChange: String, // 전일 대비
    @SerialName("prdy_vrss_sign")
    val priceChangeSign: String, // 전일 대비 부호
    @SerialName("prdy_ctrt")
    val priceChangeRate: String, // 전일 대비율
    @SerialName("stck_prdy_clpr")
    val previousPrice: String, // 전일 종가
    @SerialName("acml_vol")
    val volume: String, // 누적 거래량
    @SerialName("acml_tr_pbmn")
    val volumeAmount: String, // 누적 거래 대금
    @SerialName("hts_kor_isnm")
    val nameKr: String, // HTS 한글 종목명
    @SerialName("stck_prpr")
    val price: String, // 주식 현재가
    @SerialName("stck_shrn_iscd")
    val code: String, // 주식 단축 종목코드
    @SerialName("prdy_vol")
    val previousVolume: String, // 전일 거래량
    @SerialName("stck_mxpr")
    val maxPrice: String, // 상한가
    @SerialName("stck_llam")
    val minPrice: String, // 하한가
    @SerialName("stck_oprc")
    val `open`: String, // 시가
    @SerialName("stck_hgpr")
    val high: String, // 최고가
    @SerialName("stck_lwpr")
    val low: String, // 최저가
    @SerialName("stck_prdy_oprc")
    val previousOpen: String, // 전일 시가
    @SerialName("stck_prdy_hgpr")
    val previousHigh: String, // 전일 최고가
    @SerialName("stck_prdy_lwpr")
    val previousLow: String, // 전일 최저가
    // askp	매도호가	String	Y	10	매도호가
    // bidp	매수호가	String	Y	10	매수호가
    // prdy_vrss_vol	전일 대비 거래량	String	Y	10	전일 대비 거래량
    // vol_tnrt	거래량 회전율	String	Y	11	거래량 회전율
    // stck_fcam	주식 액면가	String	Y	11	주식 액면가
    // lstn_stcn	상장 주수	String	Y	18	상장 주수
    // cpfn	자본금	String	Y	22	자본금
    // hts_avls	시가총액	String	Y	18	HTS 시가총액
    val per: String,
    val eps: String,
    val pbr: String,
    // itewhol_loan_rmnd_ratem name	전체 융자 잔고 비율	String	Y	13	전체 융자 잔고 비율
)

@Serializable
data class DailyPrice(
    @SerialName("stck_bsop_date")
    val date: String?, // yyyyMMdd
    @SerialName("stck_clpr")
    val close: String?, // 주식 종가
    @SerialName("stck_oprc")
    val `open`: String?, // 시가
    @SerialName("stck_hgpr")
    val high: String?, // 고가
    @SerialName("stck_lwpr")
    val low: String?, // 저가
    @SerialName("acml_vol")
    val volume: String?, // 누적 거래량
    @SerialName("acml_tr_pbmn")
    val volumeAmount: String?, // 누적 거래 대금
    /*
    -flng_cls_code	락 구분 코드	String	Y	2	00:해당사항없음(락이 발생안한 경우)
    01:권리락
    02:배당락
    03:분배락
    04:권배락
    05:중간(분기)배당락
    06:권리중간배당락
    07:권리분기배당락
    -prtt_rate	분할 비율	String	Y	11	분할 비율
    -mod_yn	분할변경여부	String	Y	1	Y, N
    -revl_issu_reas	재평가사유코드	String	Y	2	재평가사유코드
    */
    @SerialName("prdy_vrss_sign")
    val changeSign: String?, // 전일 대비 부호
    @SerialName("prdy_vrss")
    val change: String?, // 전일 대비
)

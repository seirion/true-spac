package com.trueedu.spac.api.model.dto.price

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PriceResponse(
    val output: PriceDetail?,
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드 - "MCA00000"
    val msg1: String, // 응답메세지 - "정상처리 되었습니다."
)

@Serializable
data class PriceDetail(
    @SerialName("iscd_stat_cls_code")
    @Serializable(with = StockStateSerializer::class)
    val stockState: StockState, // 종목 상태 코드
    @SerialName("marg_rate")
    val marginRate: String, // 증거금 비율

    @SerialName("rprs_mrkt_kor_name")
    val nameKr: String, // 대표 시장 한글 명

    @SerialName("new_hgpr_lwpr_cls_code")
    val newHighLow: String?, // 신 고가 저가 구분 코드 조회하는 종목이 신고/신저에 도달했을 경우에만 조회됨
    @SerialName("bstp_kor_isnm")
    val sectorNameKr: String?, // 업종 한글 종목명
    @SerialName("temp_stop_yn")
    val tempStop: String, // 임시 정지 여부

    @SerialName("stck_prpr")
    val price: String, // 현재가
    @SerialName("prdy_vrss")
    val priceChange: String, // 전일 대비
    @SerialName("prdy_vrss_sign")
    @Serializable(with = PriceChangeSignSerializer::class)
    val priceChangeSign: PriceChangeSign, // 전일 대비 부호
    @SerialName("prdy_ctrt")
    val priceChangeRate: String, // 전일 대비 등락률
    @SerialName("acml_tr_pbmn")
    val volumePrice: String, // 누적 거래대금
    @SerialName("acml_vol")
    val volume: String, // 누적 거래량
    @SerialName("stck_oprc")
    val open: String, // 시가
    @SerialName("stck_hgpr")
    val high: String, // 고가
    @SerialName("stck_lwpr")
    val low: String, // 저가
    @SerialName("stck_sdpr")
    val previousClosePrice: String, // 기준가(전일종가 or 시가)
)

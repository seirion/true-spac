package com.trueedu.spac.api.model.dto.rank

import com.trueedu.spac.api.model.dto.common.ApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VolumeRankingResponse(
    val output: List<VolumeRankingOutput>,
    @SerialName("rt_cd")
    override val rtCd: String,
    @SerialName("msg_cd")
    override val msgCd: String,
    override val msg1: String,
) : ApiResponse

@Serializable
data class VolumeRankingOutput(
    @SerialName("hts_kor_isnm")
    val nameKr: String, // HTS 한글 종목명
    @SerialName("mksc_shrn_iscd")
    val code: String, // 유가증권 단축 종목코드
    @SerialName("data_rank")
    val rank: String, // 데이터 순위
    @SerialName("stck_prpr")
    val price: String, // 주식 현재가

    @SerialName("prdy_vrss_sign")
    val priceChangeSign: String, // 전일 대비 부호 (0, 1, 2)
    @SerialName("prdy_vrss")
    val priceChange: String, // 전일 대비
    @SerialName("prdy_ctrt")
    val priceChangeRate: String, // 전일 대비 등락률
    @SerialName("acml_vol")
    val volume: String, // 누적 거래량
    @SerialName("prdy_vol")
    val prevDayVolume: String, // 전일 거래량
    @SerialName("lstn_stcn")
    val totalShares: String, // 상장 주수
    @SerialName("avrg_vol")
    val avgVolume: String, // 평균 거래량
    @SerialName("n_befr_clpr_vrss_prpr_rate")
    val priceChangeRateFromDays: String, // N일전종가대비현재가대비율
    @SerialName("vol_inrt")
    val volumeRate: String, // 거래량증가율
    @SerialName("vol_tnrt")
    val turnover: String, // 거래량 회전율
    @SerialName("nday_vol_tnrt")
    val turnoverDays: String, // N일 거래량 회전율
    @SerialName("avrg_tr_pbmn")
    val avgTurnover: String, // 평균 거래대금
    @SerialName("tr_pbmn_tnrt")
    val turnoverRatio: String, // 거래대금 회전율
    @SerialName("nday_tr_pbmn_tnrt")
    val turnoverDaysTurnover: String, // N일 거래대금 회전율
    @SerialName("acml_tr_pbmn")
    val totalTurnover: String, // 누적 거래대금
)

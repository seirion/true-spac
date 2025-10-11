package com.trueedu.spac.api.model.dto.price

import com.trueedu.spac.api.model.dto.common.ApiResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TradeResponse(
    val output1: TradeDetail,
    val output2: TradePriceDetail,
    @SerialName("rt_cd")
    override val rtCd: String,
    @SerialName("msg_cd")
    override val msgCd: String,
    override val msg1: String,
) : ApiResponse

@Serializable
data class TradeDetail(

    @SerialName("aspr_acpt_hour") // 매도호가 접수 시간
    val offerAcceptHour: String,

    // 매도호가
    @SerialName("askp1")
    val sell1: String,
    @SerialName("askp2")
    val sell2: String,
    @SerialName("askp3")
    val sell3: String,
    @SerialName("askp4")
    val sell4: String,
    @SerialName("askp5")
    val sell5: String,
    @SerialName("askp6")
    val sell6: String,
    @SerialName("askp7")
    val sell7: String,
    @SerialName("askp8")
    val sell8: String,
    @SerialName("askp9")
    val sell9: String,
    @SerialName("askp10")
    val sell10: String,

    // 매수호가
    @SerialName("bidp1")
    val buy1: String,
    @SerialName("bidp2")
    val buy2: String,
    @SerialName("bidp3")
    val buy3: String,
    @SerialName("bidp4")
    val buy4: String,
    @SerialName("bidp5")
    val buy5: String,
    @SerialName("bidp6")
    val buy6: String,
    @SerialName("bidp7")
    val buy7: String,
    @SerialName("bidp8")
    val buy8: String,
    @SerialName("bidp9")
    val buy9: String,
    @SerialName("bidp10")
    val buy10: String,

    // 매도 잔량
    @SerialName("askp_rsqn1")
    val sellQuantity1: String,
    @SerialName("askp_rsqn2")
    val sellQuantity2: String,
    @SerialName("askp_rsqn3")
    val sellQuantity3: String,
    @SerialName("askp_rsqn4")
    val sellQuantity4: String,
    @SerialName("askp_rsqn5")
    val sellQuantity5: String,
    @SerialName("askp_rsqn6")
    val sellQuantity6: String,
    @SerialName("askp_rsqn7")
    val sellQuantity7: String,
    @SerialName("askp_rsqn8")
    val sellQuantity8: String,
    @SerialName("askp_rsqn9")
    val sellQuantity9: String,
    @SerialName("askp_rsqn10")
    val sellQuantity10: String,

    // 매수 잔량
    @SerialName("bidp_rsqn1")
    val buyQuantity1: String,
    @SerialName("bidp_rsqn2")
    val buyQuantity2: String,
    @SerialName("bidp_rsqn3")
    val buyQuantity3: String,
    @SerialName("bidp_rsqn4")
    val buyQuantity4: String,
    @SerialName("bidp_rsqn5")
    val buyQuantity5: String,
    @SerialName("bidp_rsqn6")
    val buyQuantity6: String,
    @SerialName("bidp_rsqn7")
    val buyQuantity7: String,
    @SerialName("bidp_rsqn8")
    val buyQuantity8: String,
    @SerialName("bidp_rsqn9")
    val buyQuantity9: String,
    @SerialName("bidp_rsqn10")
    val buyQuantity10: String,

    @SerialName("total_askp_rsqn")
    val totalSellQuantity: String, // 총 매도호가 잔량
    @SerialName("total_bidp_rsqn")
    val totalBuyQuantity: String, // 총 매수호가 잔량
) {
    fun sells(): List<Pair<Double, Double>> {
        val p = listOf(sell1, sell2, sell3, sell4, sell5, sell6, sell7, sell8, sell9, sell10)
        val q = listOf(sellQuantity1, sellQuantity2, sellQuantity3, sellQuantity4, sellQuantity5, sellQuantity6, sellQuantity7, sellQuantity8, sellQuantity9, sellQuantity10)
        return p.zip(q).map { it.first.toDouble() to it.second.toDouble() }
            .reversed() // 매도호가는 역순으로
    }

    fun buys(): List<Pair<Double, Double>> {
        val p = listOf(buy1, buy2, buy3, buy4, buy5, buy6, buy7, buy8, buy9, buy10)
        val q = listOf(buyQuantity1, buyQuantity2, buyQuantity3, buyQuantity4, buyQuantity5, buyQuantity6, buyQuantity7, buyQuantity8, buyQuantity9, buyQuantity10)
        return p.zip(q).map { it.first.toDouble() to it.second.toDouble() }
    }
}

@Serializable
data class TradePriceDetail(
    @SerialName("stck_prpr")
    val price: String, // 주식 현재가
    @SerialName("stck_oprc")
    val `open`: String, // 시가
    @SerialName("stck_hgpr")
    val high: String, // 고가
    @SerialName("stck_lwpr")
    val low: String, // 저가

    @SerialName("stck_sdpr")
    val prevPrice: String, // 주식 기준가(전일 종가)
    @SerialName("antc_cnpr")
    val anticipatedPrice: String, // 예상 체결가 Anticipated Contract Price
    @SerialName("antc_cntg_vrss")
    val anticipatedPriceChange: String, // 예상 체결가 대비
    @SerialName("antc_cntg_prdy_ctrt")
    val anticipatedPriceChangeRate: String, // 예상 체결가 대비율
    @SerialName("antc_vol")
    val anticipatedVolume: String, // 예상 체결수량

    @SerialName("stck_shrn_iscd")
    val code: String,
)

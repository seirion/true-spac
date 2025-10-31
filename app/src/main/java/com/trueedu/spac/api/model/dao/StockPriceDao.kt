package com.trueedu.spac.api.model.dao

data class StockPriceDao(
    val nameKr: String = "",
    val price: String = "",
    val priceChange: String = "", // 전일 대비
    val priceChangeRate: String = "", // 전일 대비 등락률
    val volumePrice: String = "", // 누적 거래대금
    val volume: String = "", // 누적 거래량
    val open: String = "", // 시가
    val high: String = "", // 고가
    val low: String = "", // 저가
    val previousClosePrice: String = "", // 기준가(전일종가 or 시가)
)

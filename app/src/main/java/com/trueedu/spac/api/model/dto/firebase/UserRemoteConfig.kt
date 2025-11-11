package com.trueedu.spac.api.model.dto.firebase

data class UserRemoteConfig(
    val adVisible: Boolean? = null, // 광고 표시 여부
    val refundPriceVisible: Boolean? = false, // 스팩 청산 가격 표시 여부
)

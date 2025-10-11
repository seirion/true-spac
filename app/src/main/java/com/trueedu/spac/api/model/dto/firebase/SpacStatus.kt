package com.trueedu.spac.api.model.dto.firebase

data class SpacStatus(
    val code: String,
    val nameKr: String,
    val redemptionPrice: Int?,
    val status: String,
) {
    // No-argument constructor required for Firebase
    constructor() : this("000000", "", null, "")
}

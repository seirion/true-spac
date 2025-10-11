package com.trueedu.spac.api.model.dto.firebase

/**
 * firebase database 에 보유 자산을 저장하기 위한 DTO
 */
data class UserAsset(
    val code: String,
    val nameKr: String,
    val price: Double,
    val quantity: Double,
    val memo: String,
) {
    // No-argument constructor required for Firebase
    constructor() : this("000000", "", 0.0, 0.0, "")
}

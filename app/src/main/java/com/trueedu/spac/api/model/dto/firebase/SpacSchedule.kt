package com.trueedu.spac.api.model.dto.firebase

data class SpacSchedule(
    val nameKr: String,
    val note: String,
) {
    // No-argument constructor required for Firebase
    constructor() : this("", "")
}

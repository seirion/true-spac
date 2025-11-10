package com.trueedu.spac.api.model.dto.firebase

data class SpacStatus(
    val code: String,
    val nameKr: String,
    val redemptionPrice: Int?,
    val status: Status,
) {
    // No-argument constructor required for Firebase
    constructor() : this("000000", "", null, Status.NORMAL)

    enum class Status(val description: String) {
        NORMAL("일반"),
        MERGE_REVIEW("합병심사"),
        MERGE_APPROVED("합병승인"),
        DELISTING("상장폐지"),
        UNKNOWN("-"),
        ;
    }
}

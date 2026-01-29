package com.trueedu.spac.ui.merge.model

import kotlinx.serialization.Serializable

@Serializable
data class MergeSchedule(
    val nameKr: String = "",
    val code: String = "",
    val target: String = "", // 합병 대상
    // 합병반대의사통지 접수기간 (yyyyMMdd ~ yyyyMMdd)
    val dissentNoticeStartDate: String = "",
    val dissentNoticeEndDate: String = "",
    // 주식매수청구권 행사기간 (yyyyMMdd ~ yyyyMMdd)
    val appraisalRightStartDate: String = "",
    val appraisalRightEndDate: String = "",
    // 매매거래 정지예정기간 (yyyyMMdd ~ yyyyMMdd)
    val tradingHaltStartDate: String = "",
    val tradingHaltEndDate: String = "",
    // 신주의 상장예정일 (yyyyMMdd)
    val newShareListingDate: String = "",
    // 공시 URL
    val disclosureUrl: String = "",
)

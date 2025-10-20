package com.trueedu.spac.dart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DartListResponse(
    val status: String,
    val message: String,
    @SerialName("page_no")
    val pageNum: Int? = null,
    @SerialName("page_count")
    val pageCount: Int? = null,
    @SerialName("total_count")
    val totalCount: Int? = null,
    @SerialName("total_page")
    val totalPage: Int? = null,
    val list: List<DartListItem>?,
) {
    // No-argument constructor required for Firebase
    constructor() : this("", "", list = null)
}

@Serializable
data class DartListItem(
    @SerialName("corp_code")
    val corpCode: String,
    @SerialName("corp_name")
    val corpName: String,
    @SerialName("stock_code")
    val stockCode: String,
    @SerialName("corp_cls")
    val stockClass: String, // "K"
    @SerialName("report_nm")
    val reportName: String,
    @SerialName("rcept_no")
    val receiptNum: String, // 접수번호
    @SerialName("flr_nm")
    val filerName: String, // 공시 제출인명
    @SerialName("rcept_dt")
    val receiptDate: String, // 공시 접수일자(YYYYMMDD)
    val rm: String, // 비고 - 조합된 문자로 각각은 아래와 같은 의미가 있음
                // 유 : 본 공시사항은 한국거래소 유가증권시장본부 소관임
                // 코 : 본 공시사항은 한국거래소 코스닥시장본부 소관임
                // 채 : 본 문서는 한국거래소 채권상장법인 공시사항임
                // 넥 : 본 문서는 한국거래소 코넥스시장 소관임
                // 공 : 본 공시사항은 공정거래위원회 소관임
                // 연 : 본 보고서는 연결부분을 포함한 것임
                // 정 : 본 보고서 제출 후 정정신고가 있으니 관련 보고서를 참조하시기 바람
                // 철 : 본 보고서는 철회(간주)되었으니 관련 철회신고서(철회간주안내)를 참고하시기 바람
) {
    // No-argument constructor required for Firebase
    constructor() : this("", "", "", "", "", "", "", "", "")
}

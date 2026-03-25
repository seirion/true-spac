package com.trueedu.spac.dart.model

/**
 * 스팩 투자자에게 중요한 공시 키워드 목록.
 * 보고서명(report_nm)에 해당 키워드가 포함되면 중요 공시로 분류.
 */
object ImportantDisclosure {

    /**
     * 최우선 — 합병 결정/취소/승인/청산 직결 이벤트
     */
    val CRITICAL = listOf(
        "합병",              // 합병 관련 모든 공시 (합병결정, 합병취소, 소멸합병 등)
        "상장예비심사결과",   // 승인/미승인 둘 다 포함
        "상장폐지",
        "청산",
        "해산사유",
    )

    /**
     * 중요 — 합병 진행 상황 및 상태 변화
     */
    val IMPORTANT = listOf(
        "예치",                     // 예치·신탁 관련 (청산가 변동 가능성)
        "관리종목",                 // 청산 리스크 신호
        "주권매매거래정지",         // 합병 진행 or 상폐 신호
        "주권매매거래정지해제",
        "임시주주총회결과",         // 합병 승인 주총 결과
    )

    /**
     * 중요 공시 여부 판단
     */
    fun isImportant(reportName: String): Boolean {
        val cleaned = reportName
            .replace("[기재정정]", "")
            .replace("[첨부추가]", "")
            .replace("[첨부정정]", "")
            .replace("[연장결정]", "")
            .trim()
        return (CRITICAL + IMPORTANT).any { keyword -> keyword in cleaned }
    }

    /**
     * 최우선 공시 여부 (강조 표시용)
     */
    fun isCritical(reportName: String): Boolean {
        val cleaned = reportName
            .replace("[기재정정]", "")
            .replace("[첨부추가]", "")
            .replace("[첨부정정]", "")
            .replace("[연장결정]", "")
            .trim()
        return CRITICAL.any { keyword -> keyword in cleaned }
    }
}

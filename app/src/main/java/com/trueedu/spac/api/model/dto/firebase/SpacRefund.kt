package com.trueedu.spac.api.model.dto.firebase

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class SpacRefund(
    val code: String? = null,
    val nameKr: String = "",
    val rate1: Double? = null,
    val rate2: Double? = null,
    val rate3: Double? = null,
    val listingDate: LocalDate = LocalDate.now(), // 상장일
    val endDate: LocalDate = LocalDate.now(), // 청산일(예상)
    val status: Status = Status.NORMAL,
) {

    /**
     * 청산 금액 (원금 + 수익)
     */
    fun settlementAmount(): Double? {
        if (rate1 == null || rate2 == null || rate3 == null) {
            return null
        }

        // 이자 소득세 15.4%, 신탁사 수수료 0.8%
        val incomeTaxRate = 0.154  // 수익에 대한 소득세
        val trustFeeRate = 0.008   // 원금에 대한 신탁사 수수료 0.8%
        // 1만원 짜리 스팩은 없으므로 당분간 2000원으로 고정
        var principal = 2000.0

        // rate1 적용 (1년차)
        rate1.let { r1 ->
            val interest = principal * r1  // 수익
            val incomeTax = interest * incomeTaxRate  // 수익에 대한 소득세
            val trustFee = principal * trustFeeRate  // 원금에 대한 신탁사 수수료
            principal = principal + interest - incomeTax - trustFee
        }

        // rate2 적용 (2년차)
        rate2.let { r2 ->
            val interest = principal * r2  // 수익
            val incomeTax = interest * incomeTaxRate  // 수익에 대한 소득세
            val trustFee = principal * trustFeeRate  // 원금에 대한 신탁사 수수료
            principal = principal + interest - incomeTax - trustFee
        }

        // rate3 적용 (3년차 일할 계산)
        rate3.let { r3 ->
            val totalDays = ChronoUnit.DAYS.between(listingDate, endDate)
            val remainingDays = totalDays - 730 // 2년(730일) 제외
            val ratio = remainingDays / 365.0
            val interest = principal * r3 * ratio  // 수익 (일할 계산)
            val incomeTax = interest * incomeTaxRate  // 수익에 대한 소득세
            val trustFee = principal * trustFeeRate * ratio  // 원금에 대한 신탁사 수수료 (일할 계산)
            principal = principal + interest - incomeTax - trustFee
        }

        // 최종 금액 (원금 + 수익)
        return principal
    }

    /**
     * 청산 수익률 (%)
     */
    fun returnRate(): Double? {
        val finalAmount = settlementAmount() ?: return null
        return (finalAmount - 2000.0) / 2000.0 * 100
    }

    /**
     * 청산 정보를 표시할지 여부
     * NORMAL 또는 MERGE_CANCELED 상태일 때만 표시
     */
    fun shouldShowRedemption(): Boolean {
        return status == Status.NORMAL || status == Status.MERGE_CANCELED
    }

    enum class Status(val description: String) {
        NORMAL("일반"),
        MERGE_REVIEW("합병심사"),
        MERGE_APPROVED("합병승인"),
        MERGE_CANCELED("합병취소"),
        DELISTING("상장폐지"),
        UNKNOWN("-"),
        ;
    }

    companion object {
        /**
         * 탭으로 필드가 구분된 스트링으로부터 변환하기
         * 2022-12-16	442770	IBKS제21호스팩	0	0	0	2025-11-10	MERGE_APPROVED
         */
        fun from(str: String): SpacRefund {
            val parts = str.split("\t")
            return SpacRefund(
                code = parts.getOrNull(1),
                nameKr = parts.getOrNull(2) ?: "",
                rate1 = parts.getOrNull(3)?.toDoubleOrNull(),
                rate2 = parts.getOrNull(4)?.toDoubleOrNull(),
                rate3 = parts.getOrNull(5)?.toDoubleOrNull(),
                listingDate = parts.getOrNull(0)?.let { LocalDate.parse(it) } ?: LocalDate.now(),
                endDate = parts.getOrNull(6)?.let { LocalDate.parse(it) } ?: LocalDate.now(),
                status = parts.getOrNull(7)?.let { statusStr ->
                    try {
                        Status.valueOf(statusStr)
                    } catch (e: IllegalArgumentException) {
                        Status.UNKNOWN
                    }
                } ?: Status.UNKNOWN,
            )
        }
    }
}

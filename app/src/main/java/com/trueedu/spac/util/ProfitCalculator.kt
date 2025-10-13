package com.trueedu.spac.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 청산 가치, 1년 환산 청산 가치 계산
 */
fun redemptionProfitRate(
    currentPrice: Double,
    redemptionPrice: Int,
    targetDate: LocalDate,
): Pair<Double?, Double?> {
    val now = LocalDate.now()
    val daysBetween = ChronoUnit.DAYS.between(now, targetDate)
    if (daysBetween <= 0) return null to null

    // 1년 환산 수익률로 변환하기
    val profitRate = (redemptionPrice - currentPrice) / currentPrice * 100
    val annualizedProfit = profitRate * 365 / daysBetween
    return profitRate to annualizedProfit
}

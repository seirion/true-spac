package com.trueedu.spac.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.pow

/**
 * 청산 수익률, 1년 환산 청산 수익률 계산
 *
 * @param currentPrice 현재 가격
 * @param redemptionPrice 청산 가격
 * @param targetDate 청산 예정일
 * @return Pair(청산 수익률(%), 연환산 수익률(%))
 */
fun redemptionProfitRate(
    currentPrice: Double,
    redemptionPrice: Double,
    targetDate: LocalDate,
): Pair<Double?, Double?> {
    // 입력값 검증
    if (currentPrice <= 0 || redemptionPrice <= 0) return null to null

    val now = LocalDate.now()
    val daysToRedemption = ChronoUnit.DAYS.between(now, targetDate)
    if (daysToRedemption <= 0) return null to null

    // 청산 수익률 계산
    val profitRate = (redemptionPrice - currentPrice) / currentPrice * 100

    // 1년 환산 수익률 계산 (복리 방식)
    val yearsToMaturity = daysToRedemption / 365.0
    val annualizedProfit = ((redemptionPrice / currentPrice).pow(1.0 / yearsToMaturity) - 1.0) * 100

    return profitRate to annualizedProfit
}

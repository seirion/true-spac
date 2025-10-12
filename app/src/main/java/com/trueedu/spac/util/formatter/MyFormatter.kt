package com.trueedu.spac.util.formatter

interface MyFormatter {
    fun format(value: Int, withSign: Boolean = false): String
    fun format(value: Long, withSign: Boolean = false): String
    fun format(value: Double, withSign: Boolean = false): String

    fun sign(value: Double): String {
        return when {
            value > 0.0 -> "+"
            else -> ""
        }
    }
    fun sign(value: Int) = sign(value.toDouble())
    fun sign(value: Long) = sign(value.toDouble())
}

// 자주 사용하는 formatter
val intFormatter = CashFormatter(0) // 정수 표시
val rateFormatter = RateFormatter(2) // 수익률 표시

package com.trueedu.spac.util.formatter

import java.text.NumberFormat
import java.util.Locale

class CashFormatter(
    decimalPlaces: Int = 0,
    private val withUnit: Boolean = false
): MyFormatter {
    private val numberInstance = getNumberFormatInstance(decimalPlaces)

    override fun format(value: Int, withSign: Boolean): String {
        val sign = if (withSign) sign(value) else ""
        val formatted = sign + numberInstance.format(value)
        return if (withUnit) "${formatted}원" else formatted
    }

    override fun format(value: Long, withSign: Boolean): String {
        val sign = if (withSign) sign(value) else ""
        val formatted = sign + numberInstance.format(value)
        return if (withUnit) "${formatted}원" else formatted
    }

    override fun format(value: Double, withSign: Boolean): String {
        val sign = if (withSign) sign(value) else ""
        val formatted = sign + numberInstance.format(value)
        return if (withUnit) "${formatted}원" else formatted
    }

    private fun getNumberFormatInstance(decimalPlaces: Int): NumberFormat {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        numberFormat.maximumFractionDigits = decimalPlaces
        numberFormat.minimumFractionDigits = decimalPlaces
        return numberFormat
    }
}

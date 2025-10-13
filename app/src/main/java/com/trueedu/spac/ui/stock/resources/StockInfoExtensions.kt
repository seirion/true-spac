package com.trueedu.spac.ui.stock.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.trueedu.spac.api.model.dto.price.PriceResponse
import com.trueedu.spac.ui.theme.ChartColor
import com.trueedu.spac.util.formatter.CashFormatter
import com.trueedu.spac.util.formatter.RateFormatter

private val cashFormatter = CashFormatter()
private val rateFormatter = RateFormatter()

/**
 * return [priceString, textColor]
 */
@Composable
fun priceChangeStr(priceResponse: PriceResponse): Pair<String, Color> {
    val priceChange = priceResponse.output!!.priceChange.toDouble()
    val rate = priceResponse.output.priceChangeRate.toDouble()
    return priceChangeStr(priceChange, rate)
}

@Composable
private fun priceChangeStr(priceChange: Double?, rate: Double?): Pair<String, Color> {
    if (priceChange != null && rate != null) {
        return "${cashFormatter.format(priceChange, true)} (" +
                rateFormatter.format(rate, true) +
                ")" to ChartColor.color(priceChange)
    } else {
        return "" to ChartColor.up
    }
}

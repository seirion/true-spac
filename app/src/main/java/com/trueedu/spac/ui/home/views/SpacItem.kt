package com.trueedu.spac.ui.home.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.api.model.dto.firebase.StockInfoKospi
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.theme.ChartColor
import com.trueedu.spac.util.formatter.dateFormat
import com.trueedu.spac.util.formatter.intFormatter
import com.trueedu.spac.util.formatter.rateFormatter

@Preview(showBackground = true)
@Composable
fun SpacItem(
    index: Int = 1,
    item: StockInfo = StockInfoKospi("003456", "삼성전자", ""),
    price: Double = 2000.0,
    priceChange: Double? = 10.0,
    volume: Long = 1234L,
    expectedProfit: Int? = null, // 청산 시 기대 수익
    expectedProfitRate: Double? = null, // 청산 시 기대 수익률(%)
    holdingNum: Double = 1.0,
    hasDisclosure: Boolean = true, // 전자 공시 존재 여부
    onClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        Column {
            Row {
                if (hasDisclosure) {
                    DisclosurePoint()
                }
                TrueText(
                    s = item.nameKr,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                )

                if (holdingNum != 0.0) {
                    Margin(2)
                    val s = intFormatter.format(holdingNum)
                    HoldingBadge(s)
                }
                if (item.isHalt) {
                    Margin(2)
                    HaltBadge()
                }
                if (item.isDesignated) {
                    Margin(2)
                    DesignatedBadge()
                }
            }
            val listingDateStr = dateFormat(item.listingDate ?: "")
            val marketCapStr = "${item.marketCap}억"
            TrueText(
                s = dateFormat("$listingDateStr • $marketCapStr"),
                fontSize = 10,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                val volumeString = intFormatter.format(volume)

                TrueText(
                    s = volumeString,
                    fontSize = 13,
                    color = MaterialTheme.colorScheme.secondary,
                )
                val redemptionPriceString =
                    if (expectedProfit != null && expectedProfitRate != null) {
                        val rateString = rateFormatter.format(expectedProfitRate, true)
                        "${intFormatter.format(expectedProfit)} (${rateString})"
                    } else {
                        "-"
                    }
                TrueText(
                    s = redemptionPriceString,
                    fontSize = 12,
                    color = ChartColor.color(expectedProfitRate ?: 0.0),
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .width(60.dp),
            ) {
                val priceString = intFormatter.format(price)
                TrueText(
                    s = priceString,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                )
                val priceChangeString = if (priceChange == null) {
                    "-"
                } else {
                    intFormatter.format(priceChange, true)
                }
                TrueText(
                    s = priceChangeString,
                    fontSize = 12,
                    color = ChartColor.color(priceChange ?: 0.0)
                )
            }
        }
    }
}

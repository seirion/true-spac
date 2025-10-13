package com.trueedu.spac.ui.following.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.home.views.DesignatedBadge
import com.trueedu.spac.ui.home.views.DisclosurePoint
import com.trueedu.spac.ui.home.views.HaltBadge
import com.trueedu.spac.ui.theme.ChartColor
import com.trueedu.spac.util.formatter.CashFormatter
import com.trueedu.spac.util.formatter.RateFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FollowingItem(
    nameKr: String,
    code: String,
    price: Double,
    prevClose: Double?,
    open: Double?,
    high: Double?,
    low: Double?,
    delta: Double?,
    rate: Double?,
    volume: Double,
    halt: Boolean,
    designated: Boolean,
    hasDisclosure: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val formatter = CashFormatter()
    val rateFormatter = RateFormatter()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                if (hasDisclosure) {
                    DisclosurePoint()
                }
                TrueText(
                    s = nameKr,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                if (halt) {
                    Margin(2)
                    HaltBadge()
                }
                if (designated) {
                    Margin(2)
                    DesignatedBadge()
                }
            }
            TrueText(
                s = "(${code})",
                fontSize = 13,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Row(
            modifier = Modifier.width(128.dp)
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // ohlc 데이터가 있으면 캔들 표시
            if (prevClose != null && open != null && high != null && low != null) {
                DrawCandle(
                    prevClose = prevClose,
                    open = open,
                    high = high,
                    low = low,
                    close = price,
                )
            } else {
                // 정렬을 위해
                Margin(1)
            }

            val priceColor = ChartColor.color(delta ?: 0.0)
            Column(
                horizontalAlignment = Alignment.End,
            ) {
                val totalValueString = formatter.format(price)
                TrueText(
                    s = totalValueString,
                    fontSize = 14,
                    fontWeight = FontWeight.W600,
                    color = priceColor,
                )

                val deltaString = if (delta != null && rate != null) {
                    val profitString = formatter.format(delta, true)
                    val profitRateString = rateFormatter.format(rate, true)
                    "$profitString ($profitRateString)"
                } else {
                    "-"
                }
                TrueText(s = deltaString, fontSize = 12, color = priceColor)
            }
        }
    }
}

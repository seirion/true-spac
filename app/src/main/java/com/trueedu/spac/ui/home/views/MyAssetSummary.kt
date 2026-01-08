package com.trueedu.spac.ui.home.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.theme.ChartColor
import com.trueedu.spac.util.formatter.cashFormatter
import com.trueedu.spac.util.formatter.intFormatter
import com.trueedu.spac.util.formatter.rateFormatter

@Composable
fun MyAssetSummary(
    assetCount: Int,
    totalPrincipal: Double,
    totalValue: Double,
    profitRate: Double,
    modifier: Modifier = Modifier,
) {
    val secondary = MaterialTheme.colorScheme.secondary

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                TrueText(
                    s = "보유 종목",
                    fontSize = 12,
                    color = secondary,
                )
                TrueText(
                    s = "${intFormatter.format(assetCount)}개",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                TrueText(
                    s = "총 원금",
                    fontSize = 12,
                    color = secondary,
                )
                TrueText(
                    s = cashFormatter.format(totalPrincipal),
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                TrueText(
                    s = "수익률",
                    fontSize = 12,
                    color = secondary,
                )
                TrueText(
                    s = rateFormatter.format(profitRate, true),
                    fontSize = 14,
                    color = ChartColor.color(profitRate),
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                TrueText(
                    s = "총 평가",
                    fontSize = 12,
                    color = secondary,
                )
                TrueText(
                    s = cashFormatter.format(totalValue),
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

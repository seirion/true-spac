package com.trueedu.spac.ui.home.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.home.model.SpacSort

@Composable
fun SpacSectionView(
    setSort: (SpacSort) -> Unit = {}
) {
    val textColor = MaterialTheme.colorScheme.secondary
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceDim
            )
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { setSort(SpacSort.ISSUE_DATE) }
                .padding(start = 12.dp)
        ) {
            TrueText(s = "종목", fontSize = 12, color = textColor)
            TrueText(s = "상장일 • 시가총액", fontSize = 12, color = textColor)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) { setSort(SpacSort.REDEMPTION_VALUE) }
                    .padding(end = 4.dp)
                    .weight(1f)
            ) {
                TrueText(s = "거래량", fontSize = 12, color = textColor)
                TrueText(s = "예상 청산가(수익)", fontSize = 12, color = textColor)
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(60.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                    ) { setSort(SpacSort.GROWTH_RATE) }
                    .padding(end = 8.dp)
            ) {
                TrueText(s = "가격", fontSize = 12, color = textColor)
            }
        }
    }
}

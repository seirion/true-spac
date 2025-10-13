package com.trueedu.spac.ui.stock.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.ui.common.DividerHorizontal
import com.trueedu.spac.ui.components.DigitInput
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.theme.ChartColor
import com.trueedu.spac.util.formatter.rateFormatter
import com.trueedu.spac.util.redemptionProfitRate
import com.trueedu.spac.util.toLocalDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun ColumnScope.SpacDetailView(
    currentPrice: Int,
    stock: StockInfo,
    //spac: SpacStatus
) {
    val redemptionPrice = "2100" // 임시
    val baseInputString = remember { mutableStateOf(TextFieldValue(currentPrice.toString())) }
    val targetInputString = remember { mutableStateOf(TextFieldValue(redemptionPrice)) }

    SpacValueSection()
    SpacValueView(
        baseInput = baseInputString,
        targetInput = targetInputString,
    )

    val listingDateStr = stock.listingDate ?: return
    val targetDate = listingDateStr.toLocalDate()!!
        .plusYears(3)
        .plusDays(-41)

    val basePrice = baseInputString.value.text.toIntOrNull() ?: 0
    val targetPrice = targetInputString.value.text.toIntOrNull() ?: 0
    val (profitRate, annualizedProfit) = redemptionProfitRate(
        basePrice.toDouble(), targetPrice, targetDate
    )
    if (profitRate == null || annualizedProfit == null) {

    } else {
        SpacDataView(
            "$targetDate 청산 시",
            rateFormatter.format(profitRate, true),
            ChartColor.color(profitRate)
        )
        val days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate)
        SpacDataView(
            "연환산 수익률 (${days}일)",
            rateFormatter.format(annualizedProfit, true),
            ChartColor.color(annualizedProfit)
        )
    }
    DividerHorizontal()
}

@Preview(showBackground = true)
@Composable
fun SpacValueSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 12.dp, 16.dp, 2.dp),
    ) {
        TrueText(
            s = "기준가격",
            fontSize = 14,
            color = MaterialTheme.colorScheme.primary,
        )
        TrueText(
            s = "청산가격",
            fontSize = 14,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun ColumnScope.SpacValueView(
    baseInput: MutableState<TextFieldValue>,
    targetInput: MutableState<TextFieldValue>,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        DigitInput(baseInput, Modifier.width(120.dp))
        DigitInput(targetInput, Modifier.width(120.dp))
    }
}

@Composable
fun ColumnScope.SpacDataView(title: String, value: String, valueColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        TrueText(
            s = title,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
        TrueText(
            s = value,
            fontSize = 16,
            color = valueColor,
        )
    }
}

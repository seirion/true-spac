package com.trueedu.spac.ui.stock.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.spac.api.model.dto.firebase.SpacRefund
import com.trueedu.spac.ui.common.DividerHorizontal
import com.trueedu.spac.ui.components.DigitInput
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.theme.ChartColor
import com.trueedu.spac.util.formatter.rateFormatter
import com.trueedu.spac.util.redemptionProfitRate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.SpacDetailView(
    currentPrice: Int,
    spacRefund: SpacRefund?,
) {
    if (spacRefund?.shouldShowRedemption() != true) return

    val redemptionPrice = spacRefund.settlementAmount()?.toInt()?.toString() ?: "2100"
    val baseInputString = remember { mutableStateOf(TextFieldValue(currentPrice.toString())) }
    val targetInputString = remember { mutableStateOf(TextFieldValue(redemptionPrice)) }

    SpacValueSection()
    SpacValueView(
        baseInput = baseInputString,
        targetInput = targetInputString,
    )

    var targetDate by remember {
        mutableStateOf(spacRefund.endDate)
    }

    var showDatePicker by remember { mutableStateOf(false) }

    val basePrice = baseInputString.value.text.toIntOrNull() ?: 0
    val targetPrice = targetInputString.value.text.toDoubleOrNull() ?: 0.0
    val (profitRate, annualizedProfit) = redemptionProfitRate(
        basePrice.toDouble(), targetPrice, targetDate
    )
    if (profitRate == null || annualizedProfit == null) {

    } else {
        SpacDataView(
            "$targetDate 청산 시",
            rateFormatter.format(profitRate, true),
            ChartColor.color(profitRate)
        ) {
            showDatePicker = true
        }
        val days = ChronoUnit.DAYS.between(LocalDate.now(), targetDate)
        SpacDataView(
            "연환산 수익률 (${days}일)",
            rateFormatter.format(annualizedProfit, true),
            ChartColor.color(annualizedProfit),
            null,
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        targetDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    TrueText(s = "확인", fontSize = 18)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    TrueText(s = "취소", fontSize = 18)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
fun ColumnScope.SpacDataView(
    title: String,
    value: String,
    valueColor: Color,
    onClick: (() -> Unit)? = null
) {
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
            modifier = Modifier
                .clickable(enabled = onClick != null) {
                    onClick?.invoke()
                }
        )
        TrueText(
            s = value,
            fontSize = 16,
            color = valueColor,
        )
    }
}

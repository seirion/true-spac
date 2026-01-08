package com.trueedu.spac.ui.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.data.stocks.SpacManager
import com.trueedu.spac.ui.common.OnOffSetting
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.components.bottomsheet.DraggableBottomSheet
import com.trueedu.spac.ui.home.model.SpacFilter

@Composable
fun FilterOptionBottomSheet(
    visible: Boolean,
    current: SpacFilter,
    hasAssets: Boolean,
    spacManager: SpacManager,
    onDismiss: () -> Unit,
    onValueChanged: (SpacFilter) -> Unit,
) {
    DraggableBottomSheet(
        showBottomSheet = visible,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterOptionBody(current, hasAssets, spacManager, onValueChanged)
        }
    }
}

@Composable
private fun ColumnScope.FilterOptionBody(
    current: SpacFilter,
    hasAssets: Boolean,
    spacManager: SpacManager,
    onValueChanged: (SpacFilter) -> Unit,
) {
    val trueAnalytics = LocalTrueAnalytics.current
    val spacAnnualProfitMode by spacManager.spacAnnualProfitMode

    TitleView()
    OnOffSetting("청산 수익률 1년 환산 표기", spacAnnualProfitMode) {
        trueAnalytics.clickToggleButton(
            "filter__spac_annual_profit__click",
            !it
        )
        spacManager.setSpacAnnualProfit(it)
    }
    OnOffSetting("1년 미만 종목만 보기", current.listedOverTwoYears) {
        trueAnalytics.clickToggleButton(
            "filter__listed_over_two_years__click",
            !it
        )
        onValueChanged(current.copy(listedOverTwoYears = it))
    }
    OnOffSetting("2,000원 이하 종목만 보기", current.underParValue) {
        trueAnalytics.clickToggleButton(
            "filter__under_par_value__click",
            !it
        )
        onValueChanged(current.copy(underParValue = it))
    }
    OnOffSetting("관심 종목만 보기", current.filterFollowing) {
        trueAnalytics.clickToggleButton(
            "filter__only_watching__click",
            !it
        )
        onValueChanged(current.copy(filterFollowing = it))
    }

    if (hasAssets) {
        OnOffSetting("내 보유 종목만 보기", current.onlyAssets) {
            trueAnalytics.clickToggleButton(
                "filter__only_assets__click",
                !it
            )
            onValueChanged(current.copy(onlyAssets = it))
        }
    }
}

@Composable
private fun TitleView() {
    TrueText(
        s = "스팩 필터 도구",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20,
        fontWeight = FontWeight.W600,
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    )
}

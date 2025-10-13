package com.trueedu.spac.ui.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.ui.common.BackStockTopBar
import com.trueedu.spac.ui.common.LoadingView
import com.trueedu.spac.ui.components.TouchIcon24
import com.trueedu.spac.ui.components.TrueText
import com.trueedu.spac.ui.settings.views.SettingItem
import com.trueedu.spac.ui.stock.resources.priceChangeStr
import com.trueedu.spac.ui.stock.views.SpacDetailView
import com.trueedu.spac.ui.theme.ChartColor
import com.trueedu.spac.util.formatter.intFormatter

@Composable
fun StockDetailScreen(
    stockId: String,
    vm: StockDetailViewModel = hiltViewModel(),
    openUrl: (String) -> Unit,
    editAssets: () -> Unit,
    onBack: () -> Unit,
) {
    val trueAnalytics = LocalTrueAnalytics.current

    LaunchedEffect(Unit) {
        vm.init(stockId)
    }

    val stockInfo by vm.stockInfo.collectAsState()
    val basePrice by vm.basePrice.collectAsState()
    val infoList by vm.infoList.collectAsState()

    if (stockInfo == null) {
        LoadingView()
        return
    }

    Scaffold(
        topBar = {
            val price = vm.currentPrice()
            val (priceChangeStr, textColor) = when {
                basePrice != null -> priceChangeStr(basePrice!!)
                else -> "" to ChartColor.up
            }

            val icon = if (vm.isFollowing()) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
            val actions: @Composable RowScope.() -> Unit = {
                TouchIcon24(icon, onClick = vm::toggleFollowing)
                TouchIcon24(Icons.Outlined.Edit, onClick = editAssets)
            }

            BackStockTopBar(
                stockInfo!!.nameKr,
                intFormatter.format(price, false),
                priceChangeStr,
                textColor,
                stockInfo!!.isHalt,
                stockInfo!!.isDesignated,
                false, // TODO: dartManager.hasDisclosure(stockInfo!!.code),
                onBack = onBack,
                actions = actions
            )
        },
        bottomBar = {
            /*
            if (remoteConfig.adVisible.value && admobManager.nativeAd.value != null) {
                NativeAdView(admobManager.nativeAd.value!!)
            }
             */
        },
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            SettingItem("기업 공시 보기", true) {
                trueAnalytics.clickButton(
                    "stock_detail__dart__click",
                    mapOf("name" to stockInfo!!.nameKr)
                )
                val code = stockInfo!!.code
                val url =
                    "https://dart.fss.or.kr/dsab001/main.do?autoSearch=true&textCrpNM=${code}"
                openUrl(url)
            }
            SpacDetailView(vm.currentPrice().toInt(), stockInfo!!)

            infoList.forEach {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    TrueText(s = it.first, fontSize = 15, color = MaterialTheme.colorScheme.primary)
                    TrueText(
                        s = it.second ?: "",
                        fontSize = 15,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        textAlign = TextAlign.End,
                    )
                }
            }
            // TODO: asset 정보 표시
        }
    }
}

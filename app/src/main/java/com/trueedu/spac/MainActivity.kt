package com.trueedu.spac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.data.config.LocalFeature
import com.trueedu.spac.data.config.LocalFeatureConfig
import com.trueedu.spac.ui.main.MainScreen
import com.trueedu.spac.ui.theme.TrueSpacTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var trueAnalytics: TrueAnalytics
    @Inject
    lateinit var localFeature: LocalFeature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(
                LocalTrueAnalytics provides trueAnalytics,
                LocalFeatureConfig provides localFeature,
            ) {
                TrueSpacTheme {
                    MainScreen()
                }
            }
        }
    }
}

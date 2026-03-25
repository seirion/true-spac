package com.trueedu.spac.ui.ads

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.trueedu.spac.R
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.data.log.logD
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdmobManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val trueAnalytics: TrueAnalytics,
) {
    //private var rewardedAd: RewardedAd? = null
    var nativeAd by mutableStateOf<NativeAd?>(null)
    private lateinit var adLoader: AdLoader

    private var job: Job? = null

    fun start() {
        // 기존 job이 있으면 취소
        job?.cancel()

        if (!::adLoader.isInitialized) {
            // 최초 초기화: adLoader 생성 + 즉시 로드
            init()
        } else {
            // 포그라운드 복귀 시 즉시 광고 로드
            loadNativeAd()
        }

        job = CoroutineScope(Dispatchers.Main).launch {
            flow {
                while (true) {
                    if (nativeAd == null) {
                        delay(1 * 60_000) // 1분
                    } else {
                        delay(5 * 60_000) // 5분
                    }
                    emit(Unit)
                }
            }
                .collect {
                    logD("update ad")
                    loadNativeAd()
                }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        nativeAd?.destroy()
        nativeAd = null
    }

    fun init() {
        adLoader = build()
        loadNativeAd()
    }

    private fun loadNativeAd() {
        if (!::adLoader.isInitialized) {
            logD("adLoader not initialized, skipping loadNativeAd")
            return
        }
        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun build(): AdLoader {
        return AdLoader.Builder(applicationContext, applicationContext.getString(R.string.native_ad_unit_id))
            .forNativeAd { ad: NativeAd ->
                logD("ad loaded")
                nativeAd?.destroy()
                nativeAd = null
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    logD("광고로딩 실패: $adError")
                    trueAnalytics.log(
                        "native_ad__load_fail",
                        mapOf(
                            "code" to adError.code,
                            "message" to adError.message,
                            "error" to adError.toString()
                        )
                    )
                }

                override fun onAdClicked() {
                    trueAnalytics.log("native_ad__click")
                    super.onAdClicked()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(
                        NativeAdOptions.ADCHOICES_TOP_RIGHT
                    ).build()
            )
            .build()
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BannerAd(adUnitId: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            factory = { context ->
                AdView(context).apply {
                    this.adUnitId = adUnitId
                    setAdSize(AdSize.BANNER)
                    loadAd(AdRequest.Builder().build())
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    this.adListener = object : AdListener() {
                        override fun onAdClicked() {
                        }

                        override fun onAdClosed() {
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e("admob", adError.toString())
                        }

                        override fun onAdImpression() {
                        }

                        override fun onAdLoaded() {
                        }

                        override fun onAdOpened() {
                        }

                        override fun onAdSwipeGestureClicked() {
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun NativeAdView(nativeAd: NativeAd) {
    val bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .background(
                color = bgColor,
                shape = RoundedCornerShape(8.dp)
            )
            .height(56.dp)
            .clickable(false) { }
    ) {
        val colorScheme = MaterialTheme.colorScheme
        AndroidView(
            factory = { context ->
                TrueNativeAdView(context).also {
                    it.setNativeAd(nativeAd, colorScheme)
                }
            },
            update = {
                it.setNativeAd(nativeAd, colorScheme)
            }
        )
    }
}

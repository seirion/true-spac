package com.trueedu.spac.ui.ads

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.trueedu.spac.R

class TrueNativeAdView(context: Context) : ConstraintLayout(context) {
    init {
        LayoutInflater.from(context).inflate(R.layout.admob_native_banner_layout, this, true)
    }

    fun setNativeAd(nativeAd: NativeAd, colorScheme: ColorScheme) {
        val nativeAdView = findViewById<NativeAdView>(R.id.nativeAdView)
        val mediaView = findViewById<MediaView>(R.id.icon)
        val title = findViewById<TextView>(R.id.title).also {
            it.setTextColor(colorScheme.primary.toArgb())
        }
        val text = findViewById<TextView>(R.id.text).also {
            it.setTextColor(colorScheme.tertiary.toArgb())
        }
        val button = findViewById<TextView>(R.id.ad_call_to_action).also {
            it.setTextColor(colorScheme.background.toArgb())
        }
        findViewById<CardView>(R.id.layout_ad_call_to_action).also {
            it.setCardBackgroundColor(colorScheme.outlineVariant.toArgb())
        }
        findViewById<TextView>(R.id.layout_ad_text).also {
            it.setTextColor(colorScheme.secondary.toArgb())
        }

        // MediaView를 사용하여 이미지/비디오 자산 표시
        nativeAdView.mediaView = mediaView
        nativeAd.mediaContent?.let { mediaContent ->
            mediaView.setMediaContent(mediaContent)
        }

        nativeAdView.headlineView = title.apply {
            this.text = nativeAd.headline
        }
        nativeAdView.bodyView = text.apply {
            this.text = nativeAd.body
        }
        nativeAdView.callToActionView = button.apply {
            this.text = nativeAd.callToAction
        }
        nativeAdView.setNativeAd(nativeAd)
    }
}

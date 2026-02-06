package com.pixeleye.gpsfieldareameasure.ui.components

import android.app.Activity
import android.util.Log

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoadCallback
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                // Test banner ad ID: ca-app-pub-3940256099942544/6300978111
                adUnitId = "ca-app-pub-3940256099942544/6300978111"
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        Log.e("BannerAdView", "Ad failed to load: ${error.message}")
                    }
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.d("BannerAdView", "Ad loaded successfully")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null
    // Test ID: ca-app-pub-3940256099942544/1033173712
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    fun loadAd(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e("InterstitialAdManager", "Ad failed to load: ${error.message}")
                interstitialAd = null
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d("InterstitialAdManager", "Ad loaded successfully")
                interstitialAd = ad
            }
        })
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadAd(activity) // Load the next one
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d("InterstitialAdManager", "Ad wasn't ready yet")
            onAdDismissed()
        }
    }
}

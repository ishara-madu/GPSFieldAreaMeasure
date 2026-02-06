package com.pixeleye.gpsfieldareameasure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pixeleye.gpsfieldareameasure.ui.MainApp
import com.pixeleye.gpsfieldareameasure.ui.theme.GPSFieldAreaMeasureTheme
import com.google.android.gms.ads.MobileAds
import com.pixeleye.gpsfieldareameasure.ui.components.InterstitialAdManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AdMob
        MobileAds.initialize(this) {
            InterstitialAdManager.loadAd(this)
        }
        
        enableEdgeToEdge()
        setContent { GPSFieldAreaMeasureTheme { MainApp() } }
    }
}

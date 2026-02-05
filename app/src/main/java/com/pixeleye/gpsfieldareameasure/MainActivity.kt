package com.pixeleye.gpsfieldareameasure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pixeleye.gpsfieldareameasure.ui.MainApp
import com.pixeleye.gpsfieldareameasure.ui.theme.GPSFieldAreaMeasureTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GPSFieldAreaMeasureTheme { MainApp() } }
    }
}

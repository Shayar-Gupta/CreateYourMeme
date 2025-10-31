package com.example.createyourmeme

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.createyourmeme.ui.navigation.AppNavHost
import com.example.createyourmeme.ui.theme.CreateYourMemeTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            CreateYourMemeTheme {
                AppNavHost()
            }
        }
    }
}

package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely initialize Start.io SDK with App ID 205257935
        try {
            val sdkClass = Class.forName("com.startapp.sdk.adsbase.StartAppSDK")
            val initMethod = sdkClass.getMethod(
                "init",
                android.content.Context::class.java,
                String::class.java,
                Boolean::class.javaPrimitiveType
            )
            initMethod.invoke(null, this, "205257935", true)
            Log.d("StartIo", "Start.io initialized with App ID 205257935 successfully")
        } catch (e: Exception) {
            Log.w("StartIo", "Start.io SDK not available in classpath, skipping dynamic init: ${e.message}")
        }

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    when (currentScreen) {
                        AppViewModel.Screen.Splash -> SplashView(viewModel)
                        AppViewModel.Screen.LangSelect -> LangSelectView(viewModel)
                        AppViewModel.Screen.Onboarding -> OnboardingView(viewModel)
                        AppViewModel.Screen.Authenticate -> AuthenticateView(viewModel)
                        else -> DashboardView(viewModel)
                    }
                }
            }
        }
    }
}

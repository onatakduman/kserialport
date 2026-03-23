package com.onatakduman.kserialport.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.onatakduman.kserialport.app.ui.components.AdaptiveNavScaffold
import com.onatakduman.kserialport.app.ui.navigation.AppNavigation
import com.onatakduman.kserialport.app.ui.theme.KSerialPortTheme
import com.onatakduman.kserialport.app.viewmodel.SerialViewModel

class MainActivity : ComponentActivity() {

    private val serialViewModel: SerialViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by serialViewModel.preferencesRepo.themeMode.collectAsState(initial = "dark")
            val dynamicColors by serialViewModel.preferencesRepo.dynamicColors.collectAsState(initial = false)

            val darkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            KSerialPortTheme(darkTheme = darkTheme, dynamicColor = dynamicColors) {
                val bgColor = MaterialTheme.colorScheme.background.toArgb()

                LaunchedEffect(darkTheme, bgColor) {
                    @Suppress("DEPRECATION")
                    window.statusBarColor = bgColor
                    @Suppress("DEPRECATION")
                    window.navigationBarColor = bgColor

                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                    controller.isAppearanceLightStatusBars = !darkTheme
                    controller.isAppearanceLightNavigationBars = !darkTheme
                }

                val windowSizeClass = calculateWindowSizeClass(this)
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdaptiveNavScaffold(
                        widthSizeClass = windowSizeClass.widthSizeClass,
                        navController = navController
                    ) { modifier ->
                        AppNavigation(
                            navController = navController,
                            serialViewModel = serialViewModel,
                            modifier = modifier
                        )
                    }
                }
            }
        }
    }
}

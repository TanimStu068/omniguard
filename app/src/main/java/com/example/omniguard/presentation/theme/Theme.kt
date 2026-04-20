package com.example.omniguard.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color.White,
    primaryContainer = PrimaryLightGreen,
    onPrimaryContainer = PrimaryDarkGreen,
    secondary = PrimaryGreen,
    onSecondary = Color.White,
    secondaryContainer = PrimaryLightGreen,
    onSecondaryContainer = PrimaryDarkGreen,
    background = BackgroundLight,
    onBackground = Neutral900,
    surface = SurfaceLight,
    onSurface = Neutral900,
    error = RiskCritical,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Neutral900,
    primaryContainer = PrimaryDarkGreen,
    onPrimaryContainer = PrimaryLightGreen,
    secondary = PrimaryGreen,
    onSecondary = Neutral900,
    secondaryContainer = PrimaryDarkGreen,
    onSecondaryContainer = PrimaryLightGreen,
    background = BackgroundDark,
    onBackground = Neutral200,
    surface = SurfaceDark,
    onSurface = Neutral200,
    error = RiskCritical,
    onError = Neutral900
)

@Composable
fun OmniGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    if (darkTheme) {
        systemUiController.setSystemBarsColor(
            color = BackgroundDark,
            darkIcons = false
        )
    } else {
        systemUiController.setSystemBarsColor(
            color = BackgroundLight,
            darkIcons = true
        )
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.omniguard.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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
    darkTheme: Boolean = true, // Force dark theme
    dynamicColor: Boolean = false, // Disable dynamic color to keep your specific dark theme
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    // Always use dark system bars
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = BackgroundDark,
            darkIcons = false
        )
    }

    // Always use your DarkColorScheme
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.chat4osu.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.chat4osu.di.Config

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF005AC0),
    background = DarkGray,
    onPrimary = Gray
)

private val LightColorScheme = lightColorScheme(
    primary = LightBlue,
    background = Color.White,
    onPrimary = Color.White
)

@Composable
fun Chat4osuTheme(
    darkTheme: Boolean = (Config.getKey("darkMode") == "true"),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insets = WindowCompat.getInsetsController(window, view)
            window.statusBarColor = if (darkTheme) DarkGray.toArgb() else Color.White.toArgb()
            window.navigationBarColor = if (darkTheme) DarkGray.toArgb() else Color.White.toArgb()
            insets.isAppearanceLightStatusBars = !darkTheme
            insets.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
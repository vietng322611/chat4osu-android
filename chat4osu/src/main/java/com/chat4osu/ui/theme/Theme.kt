package com.chat4osu.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
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
    isDarkMode: Boolean = Config.getKey("darkMode").toBoolean(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isDarkMode -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val color = Color.Transparent.toArgb()
        val context = LocalActivity.current as ComponentActivity

        view.setBackgroundColor(if (isDarkMode) DarkGray.toArgb() else White.toArgb())
        DisposableEffect(isDarkMode) {
            context.enableEdgeToEdge(
                statusBarStyle = if (!isDarkMode) {
                    SystemBarStyle.light(color, color)
                } else {
                    SystemBarStyle.dark(color)
                },
                navigationBarStyle = if (!isDarkMode) {
                    SystemBarStyle.light(color, color)
                } else {
                    SystemBarStyle.dark(color)
                }
            )

            onDispose { }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
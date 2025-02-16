package com.chat4osu.ultil

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.WindowInsetsCompat

class WindowManager {
    companion object {
        fun getNavigationBarInsets(): Int {
            return WindowInsetsCompat.Type.navigationBars()
        }

        fun getImeInsets(): Int {
            return WindowInsetsCompat.Type.ime()
        }

        fun getStatusBarInsets(): Int {
            return WindowInsetsCompat.Type.statusBars()
        }

        @Composable
        fun height(): Int {
            val configuration = LocalConfiguration.current
            return configuration.screenHeightDp
        }
        @Composable
        fun width(): Int {
            val configuration = LocalConfiguration.current
            return configuration.screenWidthDp
        }
    }
}
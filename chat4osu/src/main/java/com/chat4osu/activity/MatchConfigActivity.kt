package com.chat4osu.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chat4osu.ui.theme.Chat4osuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchConfigActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            Chat4osuTheme {
            }
        }
    }
}
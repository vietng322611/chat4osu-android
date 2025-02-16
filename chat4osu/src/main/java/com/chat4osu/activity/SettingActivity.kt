package com.chat4osu.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chat4osu.di.Config
import com.chat4osu.ui.theme.Black
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.ui.theme.DarkWhite
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : ComponentActivity() {
    private var darkTheme = mutableStateOf(false)
    private var saveCred = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        darkTheme.value = Config.getKey("darkMode").toBoolean()
        saveCred.value = Config.getKey("saveCred").toBoolean()

        setContent {
            Chat4osuTheme(darkTheme = darkTheme.value) {
                SettingsScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SettingsScreen() {
        BackHandler {
            saveConfig()
            navigateToActivity(SelectActivity())
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier
                        .height(90.dp)
                        .drawBehind {
                        drawLine(
                            color = if(darkTheme.value) DarkWhite else Black,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 4f
                        )
                    },
                    title = {
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Settings",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            saveConfig()
                            navigateToActivity(SelectActivity())
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            },

        ) { innerPadding ->
            Column (
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable DarkMode:")
                    Checkbox(
                        checked = darkTheme.value,
                        onCheckedChange = { darkTheme.value = it }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save credentials:")
                    Checkbox(
                        checked = saveCred.value,
                        onCheckedChange = { saveCred.value = it }
                    )
                }
            }
        }
    }

    private fun saveConfig() {
        val context = application
        Config.writeToConfig(context, "darkMode", darkTheme.value.toString())
        Config.writeToConfig(context, "saveCred", saveCred.value.toString())
    }

    private fun navigateToActivity(activity: ComponentActivity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        finish()
    }
}
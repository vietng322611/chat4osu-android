package com.chat4osu.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.chat4osu.di.AppModule
import com.chat4osu.ui.theme.Chat4osuTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class SelectActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Chat4osuTheme {
                ChatSelectScreen()
            }
        }
    }

    @Composable
    fun ChatSelectScreen() {
        var showDialog by remember { mutableStateOf(false) }

        Scaffold (
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                ) {
                    Icon(Icons.Filled.Add, "Add chat")
                }
            },
            floatingActionButtonPosition = FabPosition.End,

            content = {
                innerPadding -> Column (
                    modifier = Modifier.padding(innerPadding)
                ) {
                    AppModule.socket.getAllChat().let {
                        for (name: String in it) {
                            SwipeableButton(name = name)
                        }
                    }
                }
            }
        )

        if (showDialog) {
            ShowDialog(
                onDismiss = { showDialog = false },
                onSubmit = { input ->
                    AppModule.socket.readInput("/join $input")
                    navigateToChat()
                }
            )
        }
    }

    @OptIn(ExperimentalWearMaterialApi::class)
    @Composable
    fun SwipeableButton(name: String) {
        val squareSize = 56.dp

        val swipeState = rememberSwipeableState(0)
        val sizePx = with(LocalDensity.current) { squareSize.toPx() }
        val anchors = mapOf(0f to 0, sizePx*2 to 1)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .swipeable(
                    state = swipeState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Horizontal
                )
        ) {
            Button(
                onClick = {
                    AppModule.socket.setActiveChat(name)
                    navigateToChat()
                },
                modifier = Modifier
                    .offset { IntOffset(swipeState.offset.value.roundToInt(), 0) }
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Text(text = name)
            }

            if (swipeState.currentValue == 1) {
                IconButton(onClick = { /* do something */ }) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Delete chat"
                    )
                }
            }
        }
    }

    @Composable
    fun ShowDialog(
        onDismiss: () -> Unit,
        onSubmit: (String) -> Unit
    ) {
        var text by remember { mutableStateOf(TextFieldValue()) }

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text("Enter Your Input") },
            text = {
                TextField(
                    value = text,
                    onValueChange = { newText -> text = newText },
                    label = { Text("Enter channel name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmit(text.text)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
            }
        )
    }

    private fun navigateToChat() {
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
    }
}
package com.chat4osu.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.chat4osu.di.AppModule
import com.chat4osu.ui.theme.Chat4osuTheme
import dagger.hilt.android.AndroidEntryPoint

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
                            Button(
                                onClick = {
                                    AppModule.socket.setActiveChat(name)
                                    navigateToChat()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                            ) {
                                Text(text = name)
                            }
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
        finish()
    }
}
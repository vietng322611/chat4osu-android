package com.chat4osu.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chat4osu.di.Config
import com.chat4osu.di.SocketData
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.viewmodel.SelectViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            Chat4osuTheme {
                ChatSelectScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatSelectScreen() {
        BackHandler { navigateToActivity(LoginActivity()) }

        val selectVM: SelectViewModel = viewModel()

        val chatList by selectVM.chatList

        var showDialog by remember { mutableStateOf(false) }

        Scaffold (
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier
                        .height(90.dp)
                        .drawBehind {
                            drawLine(
                                color = if(Config.getKey("darkMode") == "true") Color.White else Color.Black,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 4f
                            )
                        },
                    title = {
                        Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                            Text(
                                "Select channel",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                navigateToActivity(LoginActivity())
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    actions = {
                        Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                navigateToActivity(SettingActivity())
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                ) {
                    Icon(Icons.Filled.Add, "Add chat")
                }
            },
            floatingActionButtonPosition = FabPosition.End,

            content = {
                innerPadding -> LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    itemsIndexed(
                        chatList,
                        key = { _, item -> item.hashCode() }
                    ) { _, name ->
                        SwipeButton(
                            name = name,
                            onRemove = SocketData::removeChat,
                            onArchive = SocketData::archiveChat
                        )
                    }
                }
            }
        )

        if (showDialog) {
            ShowDialog(
                onDismiss = { showDialog = false },
                onSubmit = { input ->
                    SocketData.readInput("/join $input")
                    navigateToActivity(ChatActivity())
                }
            )
        }
    }

    @Composable
    fun DismissBackground(dismissState: SwipeToDismissBoxState) {
        val state = when (dismissState.dismissDirection) {
            SwipeToDismissBoxValue.StartToEnd -> true
            SwipeToDismissBoxValue.EndToStart -> false
            SwipeToDismissBoxValue.Settled -> null
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (state == true) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
            Spacer(modifier = Modifier)
            if (state == false) {
                Icon(
                    Icons.Default.SaveAs,
                    contentDescription = "Save chat log"
                )
            }
        }
    }

    @Composable
    fun SwipeButton(
        name: String,
        onRemove: (String) -> Unit,
        onArchive: (String) -> String
    ) {
        val context = LocalContext.current
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = {
                when (it) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onRemove(name)
                        Toast.makeText(context, "Chat Deleted: $name", Toast.LENGTH_SHORT).show()
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        val path: String = onArchive(name)
                        Toast.makeText(context, "Chat log saved at: $path", Toast.LENGTH_SHORT).show()
                    }
                    SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
                }
                return@rememberSwipeToDismissBoxState true
            },
            positionalThreshold = { it * .4f }
        )
        SwipeToDismissBox(
            state = dismissState,
            modifier = Modifier.padding(5.dp),
            backgroundContent = { DismissBackground(dismissState) },
            content = {
                Button(
                    modifier = Modifier.fillMaxSize(),
                    onClick = {
                        SocketData.setActiveChat(name)
                        navigateToActivity(ChatActivity())
                    }
                ) {
                    Text(text = name)
                }
            }
        )
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

    private fun navigateToActivity(activity: ComponentActivity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        finish()
    }
}
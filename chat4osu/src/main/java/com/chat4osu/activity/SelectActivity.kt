package com.chat4osu.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chat4osu.config.Config
import com.chat4osu.ui.theme.Black
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.ui.theme.DarkWhite
import com.chat4osu.ui.theme.Red
import com.chat4osu.utils.Utils.Companion.InputDialog
import com.chat4osu.utils.Utils.Companion.showToast
import com.chat4osu.viewmodel.SelectViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectActivity: ComponentActivity() {
    private val selectVM: SelectViewModel by viewModels()

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
        BackHandler { logout() }

        val isDarkMode = Config.getKey("darkMode").toBoolean()

        val chatList by selectVM.chatList
        var showDialog by remember { mutableStateOf(false) }

        Scaffold (
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier
                        .drawBehind {
                            drawLine(
                                color = if (isDarkMode) DarkWhite else Black,
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
                            IconButton(onClick = { logout() }) {
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
                            onRemove = selectVM::removeChat,
                        )
                    }
                }
            }
        )

        if (showDialog) {
            InputDialog(
                text = "Enter channel name",
                onDismiss = { showDialog = false },
                onSubmit = { input ->
                    selectVM.readInput("/join $input")
                    navigateToActivity(ChatActivity())
                }
            )
        }
    }

    @Composable
    fun DismissBackground(dismissState: SwipeToDismissBoxState) {
        val color = when (dismissState.dismissDirection) {
            StartToEnd, EndToStart -> Red
            Settled -> Color.Transparent
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
                .padding(16.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            content = {}
        )
    }

    @Composable
    fun SwipeButton(
        name: String,
        onRemove: (String) -> Unit
    ) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = {
                when (it) {
                    StartToEnd, EndToStart -> {
                        onRemove(name)
                        showToast(this@SelectActivity, "Chat Deleted: $name")
                    }
                    Settled -> return@rememberSwipeToDismissBoxState false
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
                    modifier = Modifier
                        .height(45.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    onClick = {
                        selectVM.setActiveChat(name)
                        navigateToActivity(ChatActivity())
                    }
                ) {
                    Text(
                        text = name,
                        fontSize = 18.sp
                    )
                }
            }
        )
    }

    private fun logout() {
        selectVM.logout()
        navigateToActivity(LoginActivity())
    }

    private fun navigateToActivity(activity: ComponentActivity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        finish()
    }
}
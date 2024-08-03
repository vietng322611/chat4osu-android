package com.chat4osu.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chat4osu.di.AppModule
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class ChatActivity: ComponentActivity() {
    private val username = AppModule.socket.getRoot()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Chat4osuTheme {
                ChatScreen()
            }
        }
    }

    @Preview
    @SuppressLint("SimpleDateFormat")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen() {
        val chatVM: ChatViewModel = viewModel()

        val scrollTopBar = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        val scrollBottomBar = BottomAppBarDefaults.exitAlwaysScrollBehavior()
        val focusManager = LocalFocusManager.current

        val formatter = SimpleDateFormat("HH:mm:ss")

        var msg by remember {
            mutableStateOf(TextFieldValue())
        }
        val messages by chatVM.messages

        Scaffold (
            modifier = Modifier
                .nestedScroll(scrollTopBar.nestedScrollConnection)
                .fillMaxSize()
                .clickable { focusManager.clearFocus() }
                .imePadding(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            chatVM.activeChat,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            chatVM.saveMsg()
                            navigateToSelect()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = ""
                            )
                        }
                    },
                )
            },
            bottomBar = {
                BottomAppBar (
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    content = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp)
                        ) {
                            TextField(
                                value = msg,
                                onValueChange = { msg = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                placeholder = { Text("Enter your message...") },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 15.sp)
                            )
                            IconButton(
                                onClick = {
                                    chatVM.addMsg("[${formatter.format(Date())}] $username: ${msg.text}")
                                    AppModule.socket.readInput(msg.text)
                                    msg = TextFieldValue()

                                    focusManager.clearFocus()
                                },
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        }
                    },
                    scrollBehavior = scrollBottomBar
                )
            }
        ) { innerPadding -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(messages) { text ->
                SelectionContainer {
                    Text(
                        text = text,
                        fontSize = 15.sp,
                        maxLines = Int.MAX_VALUE,
                        overflow = TextOverflow.Visible,
                    )
                }
            }
        }
        }
    }

    private fun navigateToSelect() {
        val intent = Intent(this, SelectActivity::class.java)
        startActivity(intent)
        finish()
    }
}
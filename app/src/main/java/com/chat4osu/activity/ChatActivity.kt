package com.chat4osu.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chat4osu.ui.theme.Chat4osuTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class ChatActivity: ComponentActivity() {
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
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen() {
        val scrollTopBar = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        val scrollBottomBar = BottomAppBarDefaults.exitAlwaysScrollBehavior()
        var message by remember {
            mutableStateOf(TextFieldValue(""))
        }

        Scaffold (
            modifier = Modifier
                .nestedScroll(scrollTopBar.nestedScrollConnection)
                .fillMaxWidth(),
            topBar = {
                TopAppBar(
                    title = {
                        Text("#mp_12345678", // Should be channel name
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                )
            },
            bottomBar = {
                BottomAppBar (
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 3.dp, bottom = 3.dp, end = 3.dp),
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = message,
                                onValueChange = { newValue: TextFieldValue -> message = newValue },
                                placeholder = { Text(message.text) },
                                modifier = Modifier
                                    .weight(0.8f)
                                    .padding(3.dp)
                            )
                            IconButton(
                                onClick = { /* Handle send action */ },
                                modifier = Modifier.weight(0.2f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                            }
                        }
                    },
                    scrollBehavior = scrollBottomBar
                )
            }
        ) { innerPadding -> TextListScreen(
            textList = listOf(
                "Hello",
                "Hi",
                "This is a very long text that should break into multiple lines when it overflows the width of the screen.",
                "fnnuy fnnuy"
            ),
            modifier = Modifier
                .padding(innerPadding)
                .padding(5.dp)
        ) }
    }

    @SuppressLint("SimpleDateFormat")
    @Composable
    fun TextListScreen(textList: List<String>, modifier: Modifier = Modifier) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(textList) { text ->
                val formatter = SimpleDateFormat("HH:mm:ss")
                val date =  Date()
                Text(
                    text =  "[${formatter.format(date)}] Murasaki_Rie: $text",
                    fontSize = 14.sp,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier.padding(1.dp)
                )
            }
        }
    }
}


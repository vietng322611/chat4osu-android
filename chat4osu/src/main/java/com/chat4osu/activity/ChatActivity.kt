package com.chat4osu.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chat4osu.di.Config
import com.chat4osu.di.SocketData
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.ui.theme.CyanWhite
import com.chat4osu.ui.theme.LightBlue
import com.chat4osu.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity: ComponentActivity() {
    private val username = SocketData.getRoot()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
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
        val chatVM: ChatViewModel = viewModel()

        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val focusManager = LocalFocusManager.current

        var msg by remember { mutableStateOf(TextFieldValue()) }
        val messages by chatVM.messages
        val users by chatVM.users

        LaunchedEffect(listState) {
            while (true) {
                delay(500)
                val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val nextItemIndex = (lastVisibleItemIndex + 1).coerceAtMost(messages.size - 1)

                listState.animateScrollToItem(index = nextItemIndex)
            }
        }

        BackHandler {
            chatVM.saveMsg()
            navigateToActivity(SelectActivity())
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                ModalNavigationDrawer(
                modifier = Modifier.imePadding(),
                drawerState = drawerState,
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ModalDrawerSheet(
                            modifier = Modifier.width(180.dp)
                        ) {
                            Text(
                                text = "Online players: ${users.count()}",
                                modifier = Modifier.padding(15.dp)
                            )
                            HorizontalDivider()
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp),
                            ) {
                                items(users) { text ->
                                    Text(
                                        text = text,
                                        fontSize = 15.sp,
                                        maxLines = Int.MAX_VALUE,
                                        overflow = TextOverflow.Visible
                                    )
                                }
                            }
                        }
                    }
                }
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Scaffold(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { focusManager.clearFocus() },
                        topBar = {
                            TopAppBar(
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
                                            chatVM.activeChat,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                navigationIcon = {
                                    Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                        IconButton(onClick = {
                                            chatVM.saveMsg()
                                            navigateToActivity(SelectActivity())
                                        }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    if (SocketData.getActiveChatType() != "DM") {
                                        Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                                            IconButton(onClick = {
                                                chatVM.getUserList()
                                                coroutineScope.launch {
                                                    drawerState.apply {
                                                        if (isClosed) open() else close()
                                                    }
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Menu,
                                                    contentDescription = "Show online players"
                                                )
                                            }
                                        }
                                    }
                                },
                                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                            )
                        },
                        bottomBar = {
                            BottomAppBar(
                                modifier = Modifier.height(96.dp),
                                content = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextField(
                                            value = msg,
                                            onValueChange = { msg = it },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 16.dp)
                                                .padding(4.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = TextFieldDefaults.colors(
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent
                                            ),
                                            placeholder = { Text("Enter your message...") },
                                            singleLine = true,
                                            textStyle = TextStyle(
                                                fontSize = 50.sp,
                                            )
                                        )
                                        IconButton(
                                            modifier = Modifier
                                                .padding(vertical = 16.dp)
                                                .padding(8.dp),
                                            onClick = {
                                                if (msg.text.isNotEmpty()) {
                                                    chatVM.addMsg("$username: ${msg.text}")
                                                    SocketData.readInput(msg.text)
                                                    msg = TextFieldValue()
                                                }
                                            },
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Send"
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    )
                    { innerPadding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(start = 8.dp, end = 8.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            items(messages) { text ->
                                SelectionContainer {
                                    Text(
                                        text = buildString(text),
                                        fontSize = 15.sp,
                                        maxLines = Int.MAX_VALUE,
                                        overflow = TextOverflow.Visible,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun buildString(text: String): AnnotatedString {
        val textList = text.split(" ").toMutableList()
        val name: String = textList[1].replace(":", "")
        val color = if (name == username) LightBlue else CyanWhite

        return buildAnnotatedString {
            append(textList[0])
            withStyle(style = SpanStyle(color = color, fontWeight = W400)) {
                append(" $name: ")
            }
            append(textList.subList(2, textList.size).joinToString(" "))
        }
    }

    private fun navigateToActivity(activity: ComponentActivity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        finish()
    }
}
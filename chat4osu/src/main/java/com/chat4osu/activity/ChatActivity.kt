package com.chat4osu.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chat4osu.di.Config
import com.chat4osu.di.SocketData
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.ui.theme.LightBlue
import com.chat4osu.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity: ComponentActivity() {
    private val username = SocketData.getRoot()
    private val chatVM: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            Chat4osuTheme {
                ChatScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ChatScreen() {
        val coroutineScope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current

        val listState = rememberLazyListState()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val isDarkMode = Config.getKey("darkMode").toBoolean()

        var msg by remember { mutableStateOf(TextFieldValue()) }
        val messages by remember { chatVM.messages }
        val users by remember { chatVM.users }

        LaunchedEffect(messages.size) {
            listState.animateScrollToItem(index = messages.size)
        }

        BackHandler {
            chatVM.saveMsg()
            navigateToActivity(SelectActivity())
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
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
                            .clickable (
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                            ) { focusManager.clearFocus() },
                        topBar = {
                            TopAppBar(
                                modifier = Modifier.drawBehind {
                                    drawLine(
                                        color = if (isDarkMode) Color.White else Color.Black,
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
                                            chatVM.activeChat,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                navigationIcon = {
                                    Box(
                                        modifier = Modifier.fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
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
                                        Box(
                                            modifier = Modifier.fillMaxHeight(),
                                            contentAlignment = Alignment.Center
                                        ) {
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
                            )
                        },
                        bottomBar = {
                            BottomAppBar(
                                contentPadding = PaddingValues(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                ),
                                content = {
                                    TextField(
                                        value = msg,
                                        onValueChange = { msg = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .padding(end = 4.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        ),
                                        placeholder = {
                                            Text(
                                                "Enter your message...",
                                                fontSize = 15.sp
                                            )
                                        },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontSize = 15.sp,
                                        )
                                    )
                                    IconButton(
                                        onClick = {
                                            chatVM.addMsg("$username: ${msg.text}")
                                            SocketData.readInput(msg.text)
                                            msg = TextFieldValue()
                                        },
                                        enabled = msg.text.isNotEmpty()
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send"
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            items(messages) { text ->
                                SelectionContainer {
                                    Text(
                                        text = buildString(text, isDarkMode),
                                        modifier = Modifier.padding(4.dp),
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
    private fun buildString(text: String, isDarkTheme: Boolean): AnnotatedString {
        val textList = text.split(" ").toMutableList()
        val name: String = textList[1].replace(":", "")
        val color = if (name == username) LightBlue else Color(0xFF466E05)
        val colorByTheme = if (isDarkTheme) Color.White else Color.Black
        return buildAnnotatedString {
            withStyle(style = SpanStyle(color = colorByTheme)) {
                append(textList[0])
            }
            withStyle(style = SpanStyle(color = color, fontWeight = W400)) {
                append(" $name: ")
            }
            withStyle(style = SpanStyle(color = colorByTheme)) {
                append(textList.subList(2, textList.size).joinToString(" "))
            }
        }
    }

    private fun navigateToActivity(activity: ComponentActivity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        finish()
    }
}
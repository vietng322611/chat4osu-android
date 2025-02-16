package com.chat4osu.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.chat4osu.ui.theme.Black
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.ui.theme.DarkWhite
import com.chat4osu.ui.theme.LightBlue
import com.chat4osu.ultil.WindowManager
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

    @Composable
    fun ChatScreen() {
        val width = WindowManager.width().dp

        val coroutineScope = rememberCoroutineScope()
        val focusManager = LocalFocusManager.current

        val listState = rememberLazyListState()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val isDarkTheme = Config.getKey("darkMode").toBoolean()

        var msg by remember { mutableStateOf(TextFieldValue()) }
        val messages by remember { chatVM.messages }
        val users by remember { chatVM.users }

        LaunchedEffect(messages.size) {
            listState.animateScrollToItem(index = messages.size)
        }

        BackHandler {
            navigateToActivity(SelectActivity())
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ModalDrawerSheet(
                            modifier = Modifier.width(width/2)
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { focusManager.clearFocus() },
                    ) {
                        Row(
                            modifier = Modifier
                                .background(Color.Black)
                                .weight(0.08f)
                                .drawBehind {
                                    drawLine(
                                        color = if (isDarkTheme) DarkWhite else Black,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 4f
                                    )
                                },
                            verticalAlignment = Alignment.Bottom,
                            content = {
                                IconButton(
                                    modifier = Modifier.padding(bottom = 5.dp),
                                    onClick = {
                                        navigateToActivity(SelectActivity())
                                    },
                                    content = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = if (isDarkTheme) DarkWhite else Black
                                        )
                                    }
                                )
                                Text(
                                    chatVM.activeChat,
                                    modifier = Modifier
                                        .padding(start = 10.dp, bottom = 15.dp)
                                        .weight(0.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontSize = 25.sp,
                                        color = if (isDarkTheme) DarkWhite else Black
                                    )
                                )
                                if (SocketData.getActiveChatType() != "DM") {
                                    IconButton(
                                        modifier = Modifier.padding(5.dp),
                                        onClick = {
                                            chatVM.fetchUserList()
                                            coroutineScope.launch {
                                                drawerState.apply {
                                                    if (isClosed) open() else close()
                                                }
                                            }
                                        },
                                        content = {
                                            Icon(
                                                imageVector = Icons.Filled.Menu,
                                                contentDescription = "Show online players",
                                                tint = if (isDarkTheme) DarkWhite else Black
                                            )
                                        }
                                    )
                                }
                            }
                        )
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(0.6f)
                                .imePadding()
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Bottom,
                            content = {
                                items(messages) { text ->
                                    SelectionContainer {
                                        Text(
                                            text = buildString(text, isDarkTheme),
                                            modifier = Modifier.padding(4.dp),
                                            fontSize = 15.sp,
                                            maxLines = Int.MAX_VALUE,
                                            overflow = TextOverflow.Visible,
                                        )
                                    }
                                }
                            }
                        )
                        Row(
                            modifier = Modifier
                                .imePadding()
                                .drawBehind {
                                drawLine(
                                    color = if (isDarkTheme) DarkWhite else Black,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 4f
                                )
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            content = {
                                TextField(
                                    value = msg,
                                    onValueChange = { msg = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    ),
                                    placeholder = { Text("Enter your message...") },
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = {
                                        SocketData.readInput(msg.text, chatVM.activeChat)
                                        msg = TextFieldValue()
                                    },
                                    enabled = msg.text.isNotEmpty(),
                                    content = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = if (isDarkTheme) DarkWhite else Black
                                        )
                                    },
                                )
                            }
                        )
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
        val colorByTheme = if (isDarkTheme) DarkWhite else Black
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

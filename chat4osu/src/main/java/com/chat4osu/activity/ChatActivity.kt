package com.chat4osu.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chat4osu.global.Config
import com.chat4osu.global.IrcData
import com.chat4osu.global.Utils.Companion.InputDialog
import com.chat4osu.global.Utils.Companion.showToast
import com.chat4osu.ui.theme.Black
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.ui.theme.DarkBlue
import com.chat4osu.ui.theme.DarkPurple
import com.chat4osu.ui.theme.DarkWhite
import com.chat4osu.ui.theme.LightBlue
import com.chat4osu.ui.theme.WhiteWhite
import com.chat4osu.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChatActivity: ComponentActivity() {
    private lateinit var username: String
    private val chatVM: ChatViewModel by viewModels()
    private val isDarkTheme = Config.getKey("darkMode").toBoolean()
    private val textSize = Config.getKey("textSize").toInt().sp

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            Chat4osuTheme {
                ChatScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        username = IrcData.getRoot()
    }

    @Preview(apiLevel = 34)
    @Composable
    fun ChatScreen() {
        val width = LocalConfiguration.current.screenWidthDp.dp
        val focusManager = LocalFocusManager.current
        val coroutineScope = rememberCoroutineScope()

        val users by remember { chatVM.users }
        val messages by remember { chatVM.messages }
        val msgInput = remember { mutableStateOf(TextFieldValue()) }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val listState = rememberLazyListState()
        val listHeightState = remember { mutableStateOf(false) }
        val isMenuVisible = remember { mutableStateOf(false) }

        val showDialog = remember { mutableStateOf(false) }

        LaunchedEffect(messages.size, listHeightState.value) {
            listState.animateScrollToItem(index = messages.size)
        }

        BackHandler {
            navigateToActivity(SelectActivity())
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        width = width,
                        users = users
                    )
                },
                gesturesEnabled = false
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            focusManager.clearFocus()
                            isMenuVisible.value = false
                          },
                    ) {
                        TopBar(
                            modifier = Modifier
                                .height(95.dp)
                                .background(if (isDarkTheme) DarkBlue else WhiteWhite)
                                .drawBehind {
                                    drawLine(
                                        color = if (isDarkTheme) DarkWhite else Black,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 4f
                                    )
                                },
                            coroutineScope = coroutineScope,
                            drawerState = drawerState,
                            chatName = chatVM.activeChat
                        )

                        MessageView(
                            modifier = Modifier
                                .weight(1f)
                                .onSizeChanged {
                                    listHeightState.value = !listHeightState.value
                                },
                            listState = listState,
                            messages = messages
                        )

                        Column(
                            modifier = Modifier
                                .background(if (isDarkTheme) DarkBlue else WhiteWhite)
                                .padding(bottom = 5.dp)
                                .navigationBarsPadding()
                                .imePadding()
                        ) {
                            BottomBar(
                                modifier = Modifier.drawBehind {
                                    drawLine(
                                        color = if (isDarkTheme) DarkWhite else Black,
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = 4f
                                    )
                                },
                                msg = msgInput,
                                isMenuVisible = isMenuVisible,
                            )
                            BottomMenu(
                                modifier = Modifier.padding(8.dp),
                                isMenuVisible = isMenuVisible,
                                showDialog = showDialog,
                                isLobby = IrcData.getActiveChatType() == "lobby"
                            )
                        }
                    }
                }
            }
        }

        if (showDialog.value) {
            InputDialog(
                text = "Add match data",
                onDismiss = { showDialog.value = false },
                onSubmit = { data ->
                    val result = chatVM.parseMatchData(data)
                    when (result) {
                        0 -> {
                            showToast(this, "Match data added successfully")
                            showDialog.value = false
                        }
                        -1 -> showToast(this, "Invalid input")
                    }
                },
                singleLine = false
            )
        }

    }

    @Composable
    fun DrawerContent(width: Dp, users: List<String>) {
        return CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            ModalDrawerSheet(
                modifier = Modifier.width(width / 2)
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

    @Composable
    fun TopBar(
        modifier: Modifier = Modifier,
        coroutineScope: CoroutineScope,
        drawerState: DrawerState,
        chatName: String = "Test"
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Bottom
        ) {
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
                chatName,
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

            if (IrcData.getActiveChatType() != "DM") {
                IconButton(
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
                            modifier = Modifier.padding(bottom = 5.dp),
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Show online players",
                            tint = if (isDarkTheme) DarkWhite else Black
                        )
                    }
                )
            }
        }
    }

    @Composable
    fun MessageView(
        modifier: Modifier = Modifier,
        listState: LazyListState,
        messages: List<String>
    ) {
        return LazyColumn(
            modifier = modifier,
            state = listState,
            verticalArrangement = Arrangement.Bottom,
            content = {
                items(messages) { text ->
                    SelectionContainer {
                        Text(
                            text = buildString(text, isDarkTheme),
                            modifier = Modifier.padding(4.dp),
                            fontSize = textSize,
                            fontWeight = W400,
                            maxLines = Int.MAX_VALUE,
                            overflow = TextOverflow.Visible,
                        )
                    }
                }
            }
        )
    }

    @Composable
    fun BottomBar(
        modifier: Modifier = Modifier,
        msg: MutableState<TextFieldValue>,
        isMenuVisible: MutableState<Boolean>
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { isMenuVisible.value = !isMenuVisible.value },
                content = {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Show online players",
                        tint = if (isDarkTheme) DarkWhite else Black
                    )
                }
            )
            TextField(
                value = msg.value,
                onValueChange = { msg.value = it },
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
                    IrcData.readInput(msg.value.text)
                    msg.value = TextFieldValue()
                },
                enabled = msg.value.text.isNotEmpty(),
                content = {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (isDarkTheme) DarkWhite else Black
                    )
                },
            )
        }
    }

    @Composable
    fun BottomMenu(
        modifier: Modifier = Modifier,
        isMenuVisible: MutableState<Boolean>,
        showDialog: MutableState<Boolean>,
        isLobby: Boolean = false
    ) {
        AnimatedVisibility(
            modifier = modifier,
            visible = isMenuVisible.value,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Row(
                modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                if (isLobby) {
                    ButtonWithDescription(
                        onClick = { showDialog.value = true },
                        description = "Add match",
                        icon = Icons.Filled.Add
                    )
                    ButtonWithDescription(
                        onClick = {
                            val path: String? = chatVM.saveMatchData()
                            if (path == null)
                                showToast(this@ChatActivity, "Failed to save match data")
                            else
                                showToast(this@ChatActivity, "Match data saved at: $path")
                        },
                        description = "Save match",
                        icon = Icons.Filled.Download
                    )
                }
                ButtonWithDescription(
                    onClick = {
                        val path: String? = chatVM.saveChatLog()
                        if (path == null)
                            showToast(this@ChatActivity, "Failed to save chat log")
                        else
                            showToast(this@ChatActivity, "Chat log saved at: $path")
                    },
                    description = "Save log",
                    icon = Icons.Filled.Save
                )
            }
        }
    }

    @Composable
    fun ButtonWithDescription(
        onClick: () -> Unit,
        description: String,
        icon: ImageVector,
    ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedIconButton(
                onClick = { onClick() },
                modifier = Modifier
                    .height(45.dp)
                    .width(45.dp),
                shape = RoundedCornerShape(5.dp),
                border = BorderStroke(2.dp, if (isDarkTheme) DarkWhite else Black),
                content = {
                    Icon(
                        icon,
                        contentDescription = description,
                        tint = if (isDarkTheme) DarkWhite else Black
                    )
                }
            )
            Text(
                text = description,
                color = if (isDarkTheme) DarkWhite else Black,
                fontSize = 10.sp)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun buildString(text: String, isDarkTheme: Boolean): AnnotatedString {
        val textList = text.split(" ").toMutableList()
        val name: String = textList[1].replace(":", "")
        val message = textList.subList(2, textList.size).joinToString(" ")
        val nameColor = if (name == username) LightBlue else Color(0xFF466E05)
        val colorByTheme = if (isDarkTheme) DarkWhite else Black
        val messageColor = if (
                message.contains(username) ||
                message.contains(username.replace("_", " "))
            ) DarkPurple else colorByTheme
        return buildAnnotatedString {
            withStyle(style = SpanStyle(color = colorByTheme)) {
                append(textList[0])
            }
            withStyle(style = SpanStyle(color = nameColor, fontWeight = W400)) {
                append(" $name: ")
            }
            withStyle(style = SpanStyle(color = messageColor)) {
                append(message)
            }
        }
    }

    private fun navigateToActivity(activity: ComponentActivity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        finish()
    }
}

package com.chat4osu.activity

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chat4osu.config.Config
import com.chat4osu.ui.theme.Black
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.ui.theme.DarkBlue
import com.chat4osu.ui.theme.DarkWhite
import com.chat4osu.ui.theme.WhiteWhite
import com.chat4osu.utils.Utils.Companion.InputDialog
import com.chat4osu.utils.Utils.Companion.showToast
import com.chat4osu.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChatActivity: ComponentActivity() {
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

    @Composable
    fun ChatScreen() {
        val width = LocalConfiguration.current.screenWidthDp.dp
        val height = LocalConfiguration.current.screenHeightDp.dp
        val focusManager = LocalFocusManager.current
        val coroutineScope = rememberCoroutineScope()

        val topBarColor = if (isDarkTheme) DarkBlue else WhiteWhite
        val bottomColor = if (isDarkTheme) DarkBlue else WhiteWhite

        val activeChat = remember { chatVM.activeChat }
        val users = remember { chatVM.users }
        val messages = remember { chatVM.messages }
        val msgInput = remember { mutableStateOf(TextFieldValue()) }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val listState = rememberLazyListState()
        val lazyListHeight = remember { mutableIntStateOf(0) }
        val isHeightStable = remember { mutableStateOf(false) }

        val isMenuVisible = remember { mutableStateOf(false) }
        val showDialog = remember { mutableStateOf(false) }

        LaunchedEffect(messages.size, isHeightStable) {
            listState.animateScrollToItem(index = messages.size)
        }

        BackHandler {
            if (drawerState.isOpen) {
                coroutineScope.launch {
                    drawerState.close()
                }
            } else {
                endActivity(SelectActivity())
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        users = users,
                        width = width
                    )
                },
                gesturesEnabled = drawerState.isOpen
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
                                .height(height*0.1235f)
                                .background(topBarColor)
                                .drawBehind {
                                    drawLine(
                                        color = if (isDarkTheme) DarkWhite else Black,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1f
                                    )
                                },
                            coroutineScope = coroutineScope,
                            drawerState = drawerState,
                            chatName = activeChat
                        )

                        MessageView(
                            modifier = Modifier
                                .weight(1f)
                                .onSizeChanged { size ->
                                    if (size.height != lazyListHeight.intValue)
                                        lazyListHeight.intValue = size.height
                                    else
                                        isHeightStable.value = !isHeightStable.value
                                },
                            listState = listState,
                            messages = messages
                        )

                        Column(
                            modifier = Modifier
                                .background(bottomColor)
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
                                        strokeWidth = 1f
                                    )
                                },
                                msg = msgInput,
                                isMenuVisible = isMenuVisible,
                                focusManager = focusManager
                            )
                            BottomMenu(
                                modifier = Modifier.padding(8.dp),
                                isMenuVisible = isMenuVisible,
                                showDialog = showDialog,
                                isLobby = chatVM.getActiveChatType() == "lobby"
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
    fun DrawerContent(users: List<String>, width: Dp) {
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
                        SelectionContainer {
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
                    endActivity(SelectActivity())
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

            if (chatVM.getActiveChatType() != "DM") {
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
        messages: List<AnnotatedString>
    ) {
        return LazyColumn(
            modifier = modifier,
            state = listState,
            verticalArrangement = Arrangement.Bottom,
            content = {
                items(messages) { text ->
                    SelectionContainer {
                        Text(
                            text = text,
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
        isMenuVisible: MutableState<Boolean>,
        focusManager: FocusManager
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
                    chatVM.readInput(msg.value.text)
                    msg.value = TextFieldValue()
                    focusManager.clearFocus()
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
                        onClick = { chatVM.saveMatchData(this@ChatActivity) },
                        description = "Save match",
                        icon = Icons.Filled.Download
                    )
                }
                ButtonWithDescription(
                    onClick = { chatVM.saveChatLog(this@ChatActivity) },
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

    private fun endActivity(activity: ComponentActivity) {
        chatVM.stopListening()
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        finish()
    }

//    private fun navigateToActivity(activity: ComponentActivity) {
//        val intent = Intent(this, activity::class.java)
//        startActivity(intent)
//    }
}

package com.chat4osu.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat4osu.config.Config
import com.chat4osu.services.ChatService
import com.chat4osu.services.MatchService
import com.chat4osu.ui.theme.Black
import com.chat4osu.ui.theme.DarkPurple
import com.chat4osu.ui.theme.DarkWhite
import com.chat4osu.ui.theme.LightBlue
import com.chat4osu.utils.Utils.Companion.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatService: ChatService,
    private val matchService: MatchService,
) : ViewModel() {
    private val isDarkTheme = Config.getKey("darkMode").toBoolean()
    private val stopChat = mutableStateOf(false)

    private var _messages = mutableStateListOf<AnnotatedString>()
    val messages: SnapshotStateList<AnnotatedString> = _messages

    private var _users = mutableStateListOf<String>()
    val users: SnapshotStateList<String> = _users

    private var username: String = chatService.nick
    val activeChat: String = chatService.activeChat

    init {
        loadInitialMessages()
        listenRealTimeMessages()
    }

    fun fetchUserList() { _users += chatService.getAllUsers() }

    private fun loadInitialMessages() {
        try {
            _messages.clear()
            val allMessage = chatService.getMessages().map {
                buildString(it, username, isDarkTheme)
            }
            _messages += allMessage
        } catch (e: Exception) {
            Log.d("ChatViewModel", "Failed to load message: ${e.message}")
        }
    }

    private fun listenRealTimeMessages() {
        viewModelScope.launch {
            while (!stopChat.value) {
                try {
                    val newMessages = chatService.getStagedMessages().map {
                        buildString(it, username, isDarkTheme)
                    }
                    _messages += newMessages
                    delay(100)
                } catch (e: Exception) {
                    Log.d("ChatViewModel", "Failed to load message: ${e.message}")
                    delay(1000)
                }
            }
        }
    }

    fun stopListening() {
        stopChat.value = true
    }

    fun getActiveChatType(): String {
        return chatService.getActiveChatType()
    }

    fun readInput(text: String) {
        chatService.readInput(text)
        matchService.readInput(text)
    }

    fun parseMatchData(data: String): Int {
        val splitData = data.split("\r")
        return matchService.parseMatchData(splitData)
    }

    fun saveChatLog(context: Context) {
        val path = chatService.saveChatLog()
        if (path == null)
            showToast(context, "Failed to save chat log")
        else
            showToast(context, "Chat log saved at: $path")
    }

    fun saveMatchData(context: Context) {
        val path = matchService.saveMatchData()
        if (path == null)
            showToast(context, "Failed to save match data")
        else
            showToast(context, "Match data saved at: $path")
    }

    @SuppressLint("SimpleDateFormat")
    private fun buildString(text: String, username: String, isDarkTheme: Boolean): AnnotatedString {
        val spaceIndex = text.indexOf(' ')
        val colonIndex = text.indexOf(':', spaceIndex)

        val timestamp = text.substring(0, spaceIndex)
        val name = text.substring(spaceIndex + 1, colonIndex)
        val message = text.substring(colonIndex + 2)

        val nameColor = if (name == username) LightBlue else Color(0xFF466E05)
        val colorByTheme = if (isDarkTheme) DarkWhite else Black
        val messageColor = if (message.contains(username) ||
            message.contains(username.replace("_", " "))
        ) DarkPurple else colorByTheme
        val messageWeight = when (messageColor) {
            DarkPurple -> W600
            else -> W400
        }

        return buildAnnotatedString {
            withStyle(style = SpanStyle(color = colorByTheme)) {
                append(timestamp)
            }
            withStyle(style = SpanStyle(color = nameColor, fontWeight = W400)) {
                append(" $name: ")
            }
            withStyle(style = SpanStyle(color = messageColor, fontWeight = messageWeight)) {
                append(message)
            }
        }
    }
}

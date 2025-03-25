package com.chat4osu.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat4osu.global.IrcData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    private var _messages = mutableStateOf(IrcData.pullAllMessage(""))
    val messages: State<List<String>> get() = _messages

    private var _users = mutableStateOf(listOf<String>())
    val users: State<List<String>> get() = _users

    val activeChat: String = IrcData.getActiveChat()

    private var _stopTracking = mutableStateOf(false)

    init { listenChatData() }

    fun fetchUserList() { _users.value = IrcData.getUser("") }

    private fun listenChatData() {
        viewModelScope.launch {
            while (!_stopTracking.value) {
                _messages.value += IrcData.pullMessage("")

                delay(100)
            }
        }
    }

    fun parseMatchData(data: String) {
        val splitData = data.split("\n")
        IrcData.parseMatchData(splitData)
    }

    fun saveMatchData(): String? {
        return null
    }
}

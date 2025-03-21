package com.chat4osu.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat4osu.di.SocketData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {
    private var _messages = mutableStateOf(SocketData.pullAllMessage(""))
    val messages: State<List<String>> get() = _messages

    private var _users = mutableStateOf(listOf<String>())
    val users: State<List<String>> get() = _users

    val activeChat: String = SocketData.getActiveChat()

    private var _stopTracking = mutableStateOf(false)

    init { listenChatData() }

    fun fetchUserList() { _users.value = SocketData.getUser("") }

    private fun listenChatData() {
        viewModelScope.launch {
            while (!_stopTracking.value) {
                _messages.value += SocketData.pullMessage("")

                delay(100)
            }
        }
    }
}

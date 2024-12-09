package com.chat4osu.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat4osu.di.SocketData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    private val mutex = Mutex()

    private var _messages = mutableStateOf(listOf<String>())
    val messages: State<List<String>> get() = _messages

    private var _users = mutableStateOf(listOf<String>())
    val users: State<List<String>> get() = _users

    val activeChat: String = SocketData.getActiveChat()

    private var _stopTracking = mutableStateOf(false)

    init { trackChatData() }

    fun addMsg(msg: String) {
        viewModelScope.launch {
            mutex.withLock {
                _messages.value += msg
            }
        }
    }

    fun saveMsg() {
        _stopTracking.value = true
        SocketData.saveMsg(_messages.value)
    }

    fun getUserList() { _users.value = SocketData.getUser("") }

    private fun trackChatData() {
        viewModelScope.launch {
            while (!_stopTracking.value) {
                _messages.value += SocketData.getMessage("")

                delay(100)
            }
        }
    }
}

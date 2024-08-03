package com.chat4osu.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat4osu.di.AppModule
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

    val activeChat: String = AppModule.socket.getActiveChat()

    private var stopReadMsg = false

    init { readMsg() }

    fun addMsg(msg: String) {
        viewModelScope.launch {
            mutex.withLock {
                _messages.value += msg
            }
        }
    }

    fun saveMsg() {
        stopReadMsg = true
        AppModule.socket.saveMsg(_messages.value)
    }

    private fun readMsg() {
        viewModelScope.launch {
            while (!stopReadMsg) {
                _messages.value += AppModule.socket.getMessage(activeChat)
                delay(100)
            }
        }
    }
}

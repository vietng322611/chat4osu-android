package com.chat4osu.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat4osu.di.SocketData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SelectViewModel @Inject constructor() : ViewModel() {

    private var _chatList = mutableStateOf(listOf<String>())
    val chatList: State<List<String>> get() = _chatList

    private var _stopTracking = mutableStateOf(false)

    init { trackJoinedChat() }

    private fun trackJoinedChat() {
        viewModelScope.launch {
            while (!_stopTracking.value) {
                _chatList.value = SocketData.getAllChat()

                delay(500)
            }
        }
    }
}
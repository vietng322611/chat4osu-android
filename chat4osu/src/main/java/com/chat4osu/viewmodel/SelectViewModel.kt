package com.chat4osu.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat4osu.services.ChatService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectViewModel @Inject constructor(
    private val chatService: ChatService
) : ViewModel() {
    private var _chatList = mutableStateOf(listOf<String>())
    val chatList: State<List<String>> get() = _chatList

    private var _stopTracking = mutableStateOf(false)

    init { trackJoinedChat() }

    fun readInput(name: String) {
        chatService.readInput(name)
    }

    fun setActiveChat(name: String) {
        chatService.setActiveChat(name)
    }

    fun logout() {
        chatService.logout()
    }

    fun removeChat(name: String) {
        chatService.part(name)
    }

    private fun trackJoinedChat() {
        viewModelScope.launch {
            while (!_stopTracking.value) {
                _chatList.value = chatService.getAllChats()
                delay(500)
            }
        }
    }
}
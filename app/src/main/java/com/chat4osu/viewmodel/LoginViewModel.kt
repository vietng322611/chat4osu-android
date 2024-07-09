package com.chat4osu.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.chat4osu.di.AppModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _loadingState = MutableLiveData<UILoadingState>()
    val loadingState: LiveData<UILoadingState> get() = _loadingState

    private fun isValidUsername(username: String): Boolean {
        return !username.contains(" ")
    }

    fun login(username: String, password: String) {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        _loadingState.value = UILoadingState.Loading

        viewModelScope.launch {
            if (isValidUsername(trimmedUsername) && trimmedPassword != "") {
                AppModule.socket.connect(username, password)

                val error: String = AppModule.socket.collect()

                _loadingState.value = UILoadingState.NotLoading

                if (error == "") {
                    _loginEvent.emit(LoginEvent.Success)
                } else {
                    _loginEvent.emit(LoginEvent.ErrorLogin(error))
                }
            } else {
                _loadingState.value = UILoadingState.NotLoading
                _loginEvent.emit(LoginEvent.ErrorInvalidInput)
            }
        }
    }

    sealed class LoginEvent {
        data object ErrorInvalidInput: LoginEvent()
        data class ErrorLogin(val error: String): LoginEvent()
        data object Success: LoginEvent()
    }

    sealed class UILoadingState {
        data object Loading: UILoadingState()
        data object NotLoading: UILoadingState()
    }
}
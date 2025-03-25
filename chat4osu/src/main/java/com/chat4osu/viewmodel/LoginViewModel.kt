package com.chat4osu.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chat4osu.global.Config
import com.chat4osu.global.IrcData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _loadingState = MutableLiveData<UILoadingState>()
    val loadingState: LiveData<UILoadingState> get() = _loadingState

    private fun isValidUsername(username: String): Boolean {
        return !username.contains(" ") && username.isNotEmpty()
    }

    fun loadCredential(): List<String> {
        val username = Config.getKey("username")
        val password = Config.getKey("password")
        val saveCred = Config.getKey("saveCred")
        return listOf(username, password, saveCred)
    }

    fun login(username: String, password: String, saveCred: Boolean) {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        _loadingState.value = UILoadingState.Loading

        viewModelScope.launch {
            if (!isValidUsername(trimmedUsername) || trimmedPassword == "") {
                _loadingState.value = UILoadingState.NotLoading
                _loginEvent.emit(LoginEvent.ErrorInvalidInput)
            }
            else {
                val code: Int = IrcData.connect(username, password)
                if (code == 0) {
                    Config.writeToConfig(context, "username", trimmedUsername, !saveCred)
                    Config.writeToConfig(context, "password", trimmedPassword, !saveCred)
                    Config.writeToConfig(context, "saveCred", saveCred.toString())

                    _loadingState.value = UILoadingState.NotLoading
                    _loginEvent.emit(LoginEvent.Success)
                }
                else {
                    val errString = when (code) {
                        1 -> "Wrong password or username"
                        2 -> "Connection timed out"
                        3 -> "Unknown error"
                        else -> ""
                    }
                    _loadingState.value = UILoadingState.NotLoading
                    _loginEvent.emit(LoginEvent.ErrorLogin(errString))
                }
            }
        }
    }

    sealed class LoginEvent {
        data object ErrorInvalidInput: LoginEvent()
        data class ErrorLogin(val error: String) : LoginEvent()
        data class ErrorSavingCredential(val error: String): LoginEvent()
        data object Success: LoginEvent()
    }

    sealed class UILoadingState {
        data object Loading: UILoadingState()
        data object NotLoading: UILoadingState()
    }
}
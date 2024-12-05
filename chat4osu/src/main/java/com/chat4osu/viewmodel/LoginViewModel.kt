package com.chat4osu.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chat4osu.SocketData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()

    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _loadingState = MutableLiveData<UILoadingState>()
    val loadingState: LiveData<UILoadingState> get() = _loadingState

    private fun isValidUsername(username: String): Boolean {
        return !username.contains(" ")
    }

    fun loadCredential(): List<String>? {
        try {
            val file = File(context.getExternalFilesDir(null), "Y3JlZGVudGlhbHM=.cfg")

            if (file.exists()) {
                val data: List<String> = file.readText().split("\n")
                if (data.size == 2)
                    return data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun login(username: String, password: String, saveCred: Boolean) {
        val trimmedUsername = username.trim()
        val trimmedPassword = password.trim()

        _loadingState.value = UILoadingState.Loading

        viewModelScope.launch {
            if (isValidUsername(trimmedUsername) && trimmedPassword != "") {
                val code: Int = SocketData.connect(username, password)
                if (code != 0) {
                    _loadingState.value = UILoadingState.NotLoading
                    _loginEvent.emit(
                        LoginEvent.ErrorLogin(SocketData.collect())
                    )
                }
            } else {
                _loadingState.value = UILoadingState.NotLoading
                _loginEvent.emit(LoginEvent.ErrorInvalidInput)
            }

            if (saveCred) {
                try {
                    val file = File(context.getExternalFilesDir(null), "Y3JlZGVudGlhbHM=.cfg")
                    Log.d("LoginViewModel", "Credentials saved at: $file")

                    FileOutputStream(file).use { output ->
                        val data = trimmedUsername + "\n" + trimmedPassword
                        output.write(data.toByteArray())

                        output.flush()
                        output.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.message?.let {
                        _loginEvent.emit(LoginEvent.ErrorSavingCredential(it))
                    }
                }
            } else {
                try {
                    val file = File(context.getExternalFilesDir(null), "Y3JlZGVudGlhbHM=.cfg")
                    if (file.exists())
                        Log.d("LoginViewModel", "Credentials deleted: ${file.delete()}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _loadingState.value = UILoadingState.NotLoading
            _loginEvent.emit(LoginEvent.Success)
        }
    }

    sealed class LoginEvent {
        data object ErrorInvalidInput: LoginEvent()
        data class ErrorLogin(val error: String): LoginEvent()
        data class ErrorSavingCredential(val error: String): LoginEvent()
        data object Success: LoginEvent()
    }

    sealed class UILoadingState {
        data object Loading: UILoadingState()
        data object NotLoading: UILoadingState()
    }
}
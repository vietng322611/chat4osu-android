package com.chat4osu.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chat4osu.R
import com.chat4osu.ui.theme.DarkGray
import com.chat4osu.ui.theme.Chat4osuTheme
import com.chat4osu.ui.theme.Gray
import com.chat4osu.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    private val loginVM: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        subscribeToEvents()
        setContent {
            Chat4osuTheme {
                LoginScreen()
            }
        }
    }

    @Composable
    fun LoginScreen() {
        BackHandler { finish() }
        val uriHandler = LocalUriHandler.current
        val focusManager = LocalFocusManager.current
        val isDarkMode = isSystemInDarkTheme()

        var username by remember {
            mutableStateOf(TextFieldValue(""))
        }
        var password by remember {
            mutableStateOf(TextFieldValue(""))
        }
        var checked by remember { mutableStateOf(false) }
        var showProgress by remember { mutableStateOf(false) }

        loginVM.loadCredential().let {
            username = TextFieldValue(it[0])
            password = TextFieldValue(it[1])
            checked = (it[2] == "true")
        }

        loginVM.loadingState.observe(this) { uiLoadingState ->
            showProgress = when (uiLoadingState) {
                is LoginViewModel.UILoadingState.Loading -> {
                    true
                }

                is LoginViewModel.UILoadingState.NotLoading -> {
                    false
                }
            }
        }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDarkMode)
                        DarkGray
                    else
                        Color.White
                )
                .padding(start = 35.dp, end = 35.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
        ) {
            val (
                logo, usernameTextField, passwordTextField, checkBox, loginBtn, getPassBtn, progressBar
            ) = createRefs()

            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = "Logo",
                modifier = Modifier
                    .height(120.dp)
                    .width(120.dp)
                    .constrainAs(logo) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top, margin = 120.dp)
                    }
            )

            OutlinedTextField(
                value = username,
                onValueChange = { newValue: TextFieldValue -> username = newValue },
                label = { Text(
                    "Enter username",
                    color = if (isDarkMode) Gray else Color.Black
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(usernameTextField) {
                        top.linkTo(logo.bottom, margin = 32.dp)
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { newValue: TextFieldValue -> password = newValue },
                visualTransformation = PasswordVisualTransformation(),
                label = { Text(
                    "Enter password",
                    color = if (isDarkMode) Gray else Color.Black
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(passwordTextField) {
                        top.linkTo(usernameTextField.bottom, margin = 16.dp)
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .constrainAs(checkBox) {
                        top.linkTo(passwordTextField.bottom, margin = 8.dp)
                    }
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
                Text(
                    "Save credentials",
                    color = if (isDarkMode) Gray else Color.Black,
                    fontWeight = W400
                )
            }

            Button(
                onClick = {
                    loginVM.login(username.text, password.text, checked)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(loginBtn) {
                        top.linkTo(checkBox.bottom, margin = 16.dp)
                    }
            ) {
                Text("Login")
            }

            Button(
                onClick = {
                    uriHandler.openUri("https://osu.ppy.sh/home/account/edit#legacy-api")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(getPassBtn) {
                        top.linkTo(loginBtn.bottom, margin = 8.dp)
                    }
            ) {
                Text("Get IRC password")
            }

            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.constrainAs(progressBar) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(getPassBtn.bottom, margin = 16.dp)
                    }
                )
            }
        }
    }

    private fun subscribeToEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginVM.loginEvent.collect { event ->
                    when (event) {
                        is LoginViewModel.LoginEvent.ErrorInvalidInput -> {
                            showToast("Invalid username/password.")
                        }

                        is LoginViewModel.LoginEvent.ErrorSavingCredential -> {
                            val errorMessage = event.error
                            showToast("Couldn't saved credential. Error: $errorMessage")
                        }

                        is LoginViewModel.LoginEvent.ErrorLogin -> {
                            val errorMessage = event.error
                            showToast("Error: $errorMessage.")
                        }

                        is LoginViewModel.LoginEvent.Success -> {
                            showToast("Login Successful.")

                            navigateToSelect()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToSelect() {
        val intent = Intent(this, SelectActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
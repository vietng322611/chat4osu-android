package com.chat4osu.global

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue

class Utils {
    companion object {
        fun showToast(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        @Composable
        fun InputDialog(
            text: String,
            onDismiss: () -> Unit,
            onSubmit: (String) -> Unit,
            singleLine: Boolean = true
        ) {
            var input by remember { mutableStateOf(TextFieldValue()) }

            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = { Text(text) },
                text = {
                    TextField(
                        value = input,
                        onValueChange = { newText -> input = newText },
                        label = { Text("Type here") },
                        singleLine = singleLine,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (input.text.isNotEmpty())
                                onSubmit(input.text)
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
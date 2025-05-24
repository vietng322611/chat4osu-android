package com.chat4osu.utils

import android.content.Context
import android.os.Environment
import android.util.Log
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

        fun saveFile(filename: String, content: String, caller: String): String? {
            val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(folder, "$filename.log")
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                fos.write(content.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: Exception) {
                Log.e("Utils", "$caller: ${e.message}")
                return null
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (e: IOException) {
                        Log.e("Utils", "$caller: Error closing output stream $e")
                    }
                }
            }

            return "$folder/$filename.log"
        }

        fun handleAction(function: Any, args: List<Any>) {
            val method = function.javaClass.methods.firstOrNull { it.name == "invoke" } ?: return
            val params = method.parameters

            val convertedArgs = params.mapIndexed { index, param ->
                when {
                    index >= args.size -> null
                    param.type == Int::class.java -> args[index].toString().toInt()
                    param.type == Char::class.java -> args[index].toString()[0]
                    else -> args[index]
                }
            }.toTypedArray()

            try {
                method.invoke(function, *convertedArgs)
            } catch (e: Exception) {
                println("Error invoking function: ${e.message}")
            }
        }
    }
}
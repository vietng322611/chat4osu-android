package com.chat4osu.di

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import java.io.File
import java.io.FileOutputStream

class Config {
    companion object {
        private val config = mutableStateMapOf(
            "darkMode" to "false",
            "saveCred" to "false",
            "username" to "",
            "password" to ""
        )

        fun getKey(key: String): String {
            config[key]?.let { return it }
            return ""
        }

        fun loadConfig(context: Context) {
            try {
                val file = File(context.getExternalFilesDir(null), "Q29uZmln=.cfg")
                if (!file.exists()) {
                    saveConfig(context)
                    Log.d("Config", "Config Created at: $file")
                    return
                }
                Log.d("Config", "Config file at: $file")

                val data = file.readText().split("\n")
                data.forEach {
                    val value: List<String> = it.split("=")
                    config[value[0]] = value[1]
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun writeToConfig(context: Context, key: String, value: String, saveTemp: Boolean = false) {
            config[key] = value
            if (!saveTemp)
                saveConfig(context)
        }

        private fun saveConfig(context: Context) {
            val file = File(context.getExternalFilesDir(null), "Q29uZmln=.cfg")

            try {
                FileOutputStream(file).use { output ->
                    var data = ""
                    config.forEach { (key, value) ->
                        data += "$key=$value\n"
                    }
                    output.write(data.toByteArray())

                    output.flush()
                    output.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
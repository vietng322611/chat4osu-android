package com.chat4osu.types

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class Channel() {
    private val lock: Lock = ReentrantLock()

    private lateinit var name: String
    private lateinit var type: String
    private var id = 0

    private val userList = ConcurrentSkipListSet<String>()
    private val message = mutableListOf<String>()
    private val messageOnStage = mutableListOf<String>()

    private val patterns = listOf(
        Regex("BanchoBot : (.*) joined in slot \\d.") to { match: String -> addUser(listOf(match)) },
        Regex("BanchoBot : (.*) left the game.") to { match: String -> removeUser(match) }
    )

    constructor(name: String) : this() {
        this.name = name
        type = if (name.startsWith("#mp_")) {
            "lobby"
        } else if (name.startsWith("#")) {
            "chat"
        } else {
            "DM"
        }
        if (type == "lobby")
            id = name.split("_")[1].toInt()
    }

    val getUser: Set<String>
        get() = userList

    val getType: String
        get() = type

    val getId: Int
        get() = id

    fun update(data: List<String?>) {
        val message = String.format(
            "[%s] %s: %s",
            currentTimeStamp, data[2], data[3]
        )

        updateMessage(message)

        for ((pattern, action) in patterns) {
            if (!pattern.matches(message)) continue

            val match = pattern.find(message)
            if (match != null) {
                action(match.groupValues[1])
                return
            }
        }
    }

    private fun updateMessage(message: String) {
        lock.lock()
        try {
            this.message.add(message)
            messageOnStage.add(message)
        } finally {
            lock.unlock()
        }
    }

    fun addUser(name: List<String>) {
        userList.addAll(name)
    }

    fun removeUser(name: String) {
        userList.remove(name)
    }

    fun pullMessage(): List<String> {
        val ret = messageOnStage.toMutableList()
        messageOnStage.clear()
        return ret
    }

    fun pullAllMessage(): List<String> {
        val ret = message.toMutableList()
        return ret
    }

    fun archiveChat(): String? {
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(folder, "$name.log")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            val content = message.joinToString("\n")
            fos.write(content.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            Log.e("Channel", "archiveChat: ${e.message}")
            return null
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    Log.e("Channel", "archiveChat: Error closing output stream")
                }
            }
        }

        return "$folder/$name.log"
    }

    companion object {
        val currentTimeStamp: String
            get() {
                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault()) //dd/MM/yyyy
                val now = Date()
                return formatter.format(now)
            }
    }
}
package com.chat4osu.types

import com.chat4osu.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class OsuChannel() {
    private val lock: Lock = ReentrantLock()

    private lateinit var name: String
    private lateinit var type: String
    private val scope = CoroutineScope(Dispatchers.IO)
    private var id = 0

    private val userList = ConcurrentSkipListSet<String>()
    private val messages = mutableListOf<String>()
    private val onStagedMessages = mutableListOf<String>()

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
        addMessage(message)
        parseAction(message)
    }

    private fun addMessage(message: String) {
        scope.launch {
            synchronized(lock) {
                messages.add(message)
                onStagedMessages.add(message)
            }
        }
    }

    private fun parseAction(message: String) {
        scope.launch {
            for ((pattern, action) in patterns) {
                if (!pattern.matches(message)) continue

                val match = pattern.find(message)
                if (match != null) {
                    action(match.groupValues[1])
                    return@launch
                }
            }
        }

    }

    fun addUser(name: List<String>) {
        userList.addAll(name)
    }

    fun removeUser(name: String) {
        userList.remove(name)
    }

    fun getStagedMessages(): List<String> {
        synchronized(lock) {
            val result = onStagedMessages.toList()
            onStagedMessages.clear()
            return result
        }
    }

    fun getMessages(): List<String> {
        synchronized(lock) {
            val result = messages.toMutableList()
            return result
        }
    }

    fun archiveChat(): String? {
        return Utils.saveFile(name, messages.joinToString("\n"), "archiveChat")
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
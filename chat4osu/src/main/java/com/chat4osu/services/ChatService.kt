package com.chat4osu.services

import android.util.Log
import com.chat4osu.osuIRC.Manager
import com.chat4osu.osuIRC.OsuSocket
import com.chat4osu.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatService @Inject constructor(
    private val osuSocket: OsuSocket
) {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    val activeChat
        get() = Manager.activeChat
    val nick
        get() = Manager.nick

    private val patterns = listOf(
        Regex("/raw (.*)") to  { text: String -> sendRaw(text) },
        Regex("/join (.*)") to  { name: String -> join(name) },
        Regex("/part #(.*)") to  { name: String -> part("#$name") },
    )

    suspend fun connect(nick: String, pass: String): Int {
        return withContext(Dispatchers.IO) {
            osuSocket.connect(nick, pass)
        }
    }

    fun logout() {
        Manager.clear()
        serviceScope.launch {
            osuSocket.logout()
        }
    }

    fun readInput(input: String) {
        for ((pattern, action) in patterns) {
            pattern.find(input)?.let { match ->
                // IDK what the fuck is this either
                Utils.handleAction(action, match.groupValues.drop(1))
                return
            }
        }
        send(input)
    }

    fun updateMessage(message: String) {
        Manager.update(listOf(
            "1",
            activeChat,
            nick,
            message
        ))
    }

    fun send(message: String) {
        updateMessage(message)
        serviceScope.launch {
            osuSocket.send("PRIVMSG $activeChat $message")
        }
    }

    private fun sendRaw(msg: String) {
        serviceScope.launch {
            osuSocket.send(msg)
        }
    }

    private fun join(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.startsWith("#")) {
            serviceScope.launch {
                osuSocket.join(trimmedName)
            }
        } else {
            Manager.addChat(trimmedName)
            setActiveChat(trimmedName)
        }
    }

    fun part(name: String) {
        serviceScope.launch {
            osuSocket.part(name)
        }
    }

    fun getActiveChatType(): String {
        Manager.getChat()?.let {
            return it.getType
        }
        Log.d("ChatService", "getActiveChatType: $activeChat not found")
        return ""
    }

    fun getAllChats() = Manager.chatList

    fun getAllUsers(): List<String> {
        Manager.getChat()?.let {
            return it.getUser.toList()
        }
        return listOf()
    }

    fun getMessages(): List<String> {
        Manager.getChat()?.let {
            return it.getMessages()
        }
        return listOf()
    }

    fun getStagedMessages(): List<String> {
        Manager.getChat()?.let {
            return it.getStagedMessages()
        }
        return listOf()
    }

    fun setActiveChat(name: String) {
        Manager.activeChat = name
    }

    fun saveChatLog(): String? {
        val outputFile = Manager.archiveChat()
        return outputFile
    }
}
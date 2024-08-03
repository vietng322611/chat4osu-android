package com.chat4osu.osu

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppSocket {
    private var osuSocket = OsuSocket()

    private val patterns = listOf(
        Regex("/raw (.*)") to { match: String -> sendRaw(match) },
        Regex("/join (.*)") to { match: String -> getDM(match) },
        Regex("/join #(.*)") to { match: String -> osuSocket.join("#$match") },
        Regex("/part #(.*)") to { match: String -> osuSocket.part("#$match") },
        Regex("!mp make (.*)") to { match: String -> createMatch(match) }
    )

    suspend fun connect(nick: String, pass: String): Int {
        return withContext(Dispatchers.IO) {
            val code = osuSocket.connect(nick, pass)
            code
        }
    }

    fun collect(): String {
        return try {
            osuSocket.stackTrace.last()
        } catch (e: NoSuchElementException) { "" }
    }

    fun readInput(input: String) {
        for ((pattern, action) in patterns) {
            if (!pattern.matches(input)) continue

            val match = pattern.find(input)
            if (match != null) {
                action(match.groupValues[1])
                println(collect())
                return
            }
        }

        send(input)
    }

    private fun send(msg: String) {
        osuSocket.send("PRIVMSG ${osuSocket.manager.activeChat} $msg")
    }

    private fun getDM(user: String) {
        osuSocket.manager.addChat(user)
        setActiveChat(user)
    }

    private fun sendRaw(msg: String) {
        osuSocket.send(msg)
    }

    private fun createMatch(name: String) {
        send("!mp make $name")
//        if (osuSocket.manager.activeChat.equals("BanchoBot")) {}
    }

    fun getMessage(channelName: String): MutableList<String> {
        osuSocket.manager.getChannel(channelName)?.let {
            return it.message
        }

        return mutableStateListOf()
    }

    fun saveMsg(msgList: List<String>) {
        osuSocket.manager.getChannel("")?.saveMsg(msgList)
    }

    fun getUser(channelName: String): Set<String> {
        osuSocket.manager.getChannel(channelName)?.let {
            return it.user
        }
        return mutableSetOf()
    }

    fun getRoot(): String {
        return osuSocket.manager.nick
    }

    fun getActiveChat(): String {
        osuSocket.manager.activeChat?.let {
            return osuSocket.manager.activeChat
        }
        return ""
    }

    fun getAllChat(): List<String> {
        return osuSocket.manager.allChat
    }

    fun setActiveChat(name: String) {
        osuSocket.manager.setActiveChat(name)
    }
}
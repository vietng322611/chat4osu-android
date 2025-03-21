package com.chat4osu.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.chat4osu.osuIRC.OsuSocket

class SocketData {
    companion object {
        private val osuSocket = OsuSocket()

        private val patterns = listOf(
            Regex("/raw (.*)") to { match: String -> sendRaw(match) },
            Regex("/join (.*)") to { match: String -> getChat(match) },
            Regex("/part #(.*)") to { match: String -> osuSocket.part("#$match") }
        )

        suspend fun connect(nick: String, pass: String): Int {
            return withContext(Dispatchers.IO) {
                osuSocket.connect(nick, pass)
            }
        }

        fun readInput(input: String, channel: String) {
            for ((pattern, action) in patterns) {
                if (!pattern.matches(input)) continue

                val match = pattern.find(input)
                if (match != null) {
                    action(match.groupValues[1])
                    return
                }
            }

            send(input, channel)
        }

        private fun send(message: String, channel: String) {
            osuSocket.manager.update(listOf(
                "1",
                channel,
                osuSocket.manager.nick,
                message
            ))
            osuSocket.send("PRIVMSG ${osuSocket.manager.activeChat} $message")
        }

        private fun getChat(name: String) {
            if (name.startsWith("#")) {
                osuSocket.join(name)
            } else {
                osuSocket.manager.addChat(name)
                setActiveChat(name)
            }
        }

        private fun sendRaw(msg: String) {
            osuSocket.send(msg)
        }

        fun pullMessage(channelName: String): List<String> {
            osuSocket.manager.getChannel(channelName)?.let {
                return it.pullMessage()
            }
            return listOf()
        }

        fun pullAllMessage(channelName: String): List<String> {
            osuSocket.manager.getChannel(channelName)?.let {
                return it.pullAllMessage()
            }
            return listOf()
        }

        fun getUser(channelName: String): List<String> {
            osuSocket.manager.getChannel(channelName)?.let {
                return it.getUser.toList()
            }
            return listOf()
        }

        fun getRoot(): String {
            return osuSocket.manager.nick
        }

        fun getActiveChat(): String {
            return osuSocket.manager.activeChat
        }

        fun getActiveChatType(): String {
            osuSocket.manager.getChannel("")?.let {
                return it.getType
            }
            return ""
        }

        fun getAllChat(): List<String> {
            return osuSocket.manager.allChat
        }

        fun setActiveChat(name: String) {
            osuSocket.manager.activeChat = name
        }

        fun archiveChat(name: String): String? {
            val outputFile = osuSocket.archiveChat(name)
            return outputFile
        }

        fun removeChat(name: String) {
            osuSocket.part(name)
        }
    }
}
package com.chat4osu.global

import com.chat4osu.osuIRC.global.Manager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.chat4osu.osuIRC.OsuSocket

class IrcData {
    companion object {
        private var osuSocket = OsuSocket()

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
            Manager.update(listOf(
                "1",
                channel,
                Manager.nick,
                message
            ))
            osuSocket.send("PRIVMSG ${Manager.activeChat} $message")
        }

        private fun sendRaw(msg: String) {
            osuSocket.send(msg)
        }

        private fun getChat(name: String) {
            if (name.startsWith("#")) {
                osuSocket.join(name)
            } else {
                Manager.addChat(name)
                setActiveChat(name)
            }
        }

        fun getRoot(): String { return Manager.nick }

        fun getActiveChat(): String { return Manager.activeChat }

        fun getActiveChatType(): String {
            Manager.getChat("")?.let {
                return it.getType
            }
            return ""
        }

        fun getAllChat(): List<String> {
            return Manager.chatList
        }

        fun getUser(channelName: String): List<String> {
            Manager.getChat(channelName)?.let {
                return it.getUser.toList()
            }
            return listOf()
        }

        fun pullMessage(channelName: String): List<String> {
            Manager.getChat(channelName)?.let {
                return it.pullMessage()
            }
            return listOf()
        }

        fun pullAllMessage(channelName: String): List<String> {
            Manager.getChat(channelName)?.let {
                return it.pullAllMessage()
            }
            return listOf()
        }

        fun setActiveChat(name: String) {
            Manager.activeChat = name
        }

        fun archiveChat(name: String): String? {
            val outputFile = Manager.archiveChat(name)
            return outputFile
        }

        fun removeChat(name: String) {
            osuSocket.part(name)
        }

        fun parseMatchData(data: List<String>) {
            Manager.parseMatchData(data)
        }
    }
}
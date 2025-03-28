package com.chat4osu.global

import android.util.Log
import com.chat4osu.global.customRegex.ReflectiveAction
import com.chat4osu.global.customRegex.UniversalAction
import com.chat4osu.osuIRC.OsuSocket
import com.chat4osu.osuIRC.global.Manager
import com.chat4osu.types.MatchData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IrcData {
    companion object {
        private var osuSocket = OsuSocket()

        private val patterns = listOf(
            Regex("/raw (.*)") to  { text: String -> sendRaw(text) },
            Regex("/join (.*)") to  { name: String -> getChat(name) },
            Regex("/part #(.*)") to  { name: String -> osuSocket.part("#$name") },
            Regex("/set") to { setMatchRule() },
            Regex("/pick (.*)") to  { pick: String -> pickMap(pick) },
            Regex("/timer (\\d+) [\\w\\s]+") to  { time: Int, text: String -> setTimer(time, text) },
            Regex("/start (\\d+) [\\w\\s]+") to  { time: Int, text: String -> startMatch(time, text) },
            Regex("/score \\d") to  { team: Int -> setScore(team) },
            Regex("/unscore \\d") to  { team: Int -> undoScore(team) },
        )

        suspend fun connect(nick: String, pass: String): Int {
            return withContext(Dispatchers.IO) {
                osuSocket.connect(nick, pass)
            }
        }

        fun readInput(input: String) {
            for ((pattern, action) in patterns) {
                val match = pattern.find(input) ?: continue
                val groups = match.groupValues.drop(1)
                when (action) {
                    is UniversalAction -> action.invoke(groups)
                    else -> ReflectiveAction(action).invoke(groups)
                }
                return
            }
            send(input)
        }

        private fun updateMessage(message: String) {
            Manager.update(listOf(
                "1",
                Manager.activeChat,
                Manager.nick,
                message
            ))
        }

        private fun send(message: String) {
            updateMessage(message)
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

        fun saveChatLog(name: String): String? {
            val outputFile = Manager.archiveChat(name)
            return outputFile
        }

        fun removeChat(name: String) {
            osuSocket.part(name)
        }

        fun parseMatchData(data: List<String>, chat: String): Int {
            return Manager.parseMatchData(data, chat)
        }

        fun logout() {
            Manager.clear()
            osuSocket.logout()
        }

        private fun getMatch(name: String): MatchData? {
            val match = Manager.getMatch(name)
            if (match == null) {
                updateMessage("Failed to get match data")
                Log.d("getMatch", "Lobby $name has no match data")
                return null
            }
            return match
        }

        private fun setMatchRule() {
            val match = getMatch(Manager.activeChat) ?: return
            send(match.matchRules)
        }

        private fun pickMap(pick: String) {
            val match = Manager.getMatch(Manager.activeChat) ?: return
            val commands = match.getPick(pick)
            if (commands == null) {
                updateMessage("Pick $pick not found")
                return
            }
            for (command in commands)
                send(command)
        }

        private fun setTimer(time: Int = 90, text: String) {
            send("!mp timer $time $text")
        }

        private fun startMatch(time: Int = 10, text: String) {
            send("!mp start $time $text")
        }

        private fun setScore(team: Int) {
            val match = getMatch(Manager.activeChat) ?: return
            if (team == 0) match.redScore = 1 // increase by 1
            else match.blueScore = 1
        }

        private fun undoScore(team: Int) {
            val match = getMatch(Manager.activeChat) ?: return
            if (team == 0) match.redScore = -1 // decrease by 1
            else match.blueScore = -1
        }
    }
}
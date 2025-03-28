package com.chat4osu.osuIRC.global

import android.util.Log
import com.chat4osu.types.Channel
import com.chat4osu.types.MatchData

class Manager {
    companion object {
        private val _chatList: MutableMap<String, Channel> = HashMap()
        private val _matchList: MutableMap<Int, MatchData> = HashMap()

        private var _nick = ""
        private var _pass = ""
        private var _activeChat = ""

        var nick: String
            get() = _nick
            set(value) { _nick = value }

        var pass: String
            get() = _pass
            set(value) { _pass = value }

        var activeChat: String
            get() = _activeChat
            set(value) { _activeChat = value }

        val chatList: List<String>
            get() = ArrayList(_chatList.keys)

        fun clear() {
            _chatList.clear()
            _matchList.clear()
            _nick = ""
            _pass = ""
            _activeChat = ""
        }

        fun getChat(name: String): Channel? {
            return if (name.isEmpty())
                _chatList[_activeChat]
            else
                _chatList[name]
        }

        fun addChat(name: String) {
            if (!_chatList.containsKey(name))
                _chatList[name] = Channel(name)
            if (_chatList[name]?.getType == "lobby") {
                val id = _chatList[name]?.getId ?: 0
                if (id != 0)
                    _matchList[id] = MatchData(id)
                else
                    Log.d("Error", "Lobby $name match ID is 0")
            }
        }

        fun removeChat(name: String) {
            _chatList.remove(name)
        }

        fun update(data: List<String>) {
            if (data.isEmpty()) return

            var name = data[1]
            try {
                when (data[0]) {
                    "0" -> { // join
                        if (data[2] == _nick) addChat(name)
                        _chatList[name]?.addUser(data.subList(2, data.size))
                    }

                    "1" -> { // message
                        if (name == _nick) name = data[2]
                        if (!_chatList.containsKey(name)) addChat(name)
                        _chatList[name]?.update(data)
                    }

                    "2" -> { // leave
                        if (data[2] == _nick) removeChat(name)
                        else _chatList[name]?.removeUser(data[2])
                    }
                }
            } catch (e: Exception) {
                Log.d("Error", e.message ?: "Unknown error")
            }
        }

        fun archiveChat(name: String): String? {
            val channel = getChat(name)
            if (channel == null) return channel

            val outputFile = channel.archiveChat()
            if (outputFile != null) {
                removeChat(name)
            }

            return outputFile
        }

        fun parseMatchData(data: List<String>, chatName: String): Int {
            val match = getMatch(chatName)
            return match?.parseMatchData(data) ?: -1
        }

        fun getMatch(chatName: String): MatchData? {
            _chatList[chatName]?.let {
                val id = it.getId
                return _matchList[id]
            }
            return null
        }
    }
}
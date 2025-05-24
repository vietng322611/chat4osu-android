package com.chat4osu.osuIRC

import android.util.Log
import com.chat4osu.types.OsuChannel
import com.chat4osu.types.MatchData

class Manager {
    companion object {
        private val _chatList = mutableMapOf<String, OsuChannel>()
        private val _matchList = mutableMapOf<Int, MatchData>()

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

        fun getChat(name: String = _activeChat): OsuChannel? {
            return _chatList[name]
        }

        fun addChat(name: String) {
            if (_chatList.containsKey(name)) return
            _chatList[name] = OsuChannel(name)
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
            if (_activeChat == name) _activeChat = ""
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

        fun archiveChat(): String? {
            val channel = getChat()
            if (channel == null) return channel

            val outputFile = channel.archiveChat()

            return outputFile
        }

        fun getMatch(): MatchData? {
            getChat()?.let {
                val id = it.getId
                return _matchList[id]
            }
            return null
        }

        fun parseMatchData(data: List<String>): Int {
            val match = getMatch()
            return match?.parseMatchData(data) ?: -1
        }

        fun archiveMatch(): String? {
            val match = getMatch() ?: return null
            return match.archiveMatch()
        }
    }
}
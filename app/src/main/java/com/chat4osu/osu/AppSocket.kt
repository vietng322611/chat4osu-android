package com.chat4osu.osu

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSocket {
    private var osuSocket = OsuSocket()
    var a = 0

    suspend fun connect(nick: String, pass: String): Int {
        return withContext(Dispatchers.IO) {
            osuSocket.connect(nick, pass)
        }
    }

    fun collect(): String {
        return try {
            osuSocket.stackTrace.last()
        } catch (e: NoSuchElementException) { "" }
    }

    fun join(channelName: String) { osuSocket.join(channelName) }

    fun part(channelName: String) { osuSocket.part(channelName) }

    fun getMessage(channelName: String): MutableList<String>? {
        val channel = osuSocket.manager.getChannel(channelName)

        if (channel != null) { return channel.getMessage() }
        else { return null }
    }

    fun getUser(channelName: String): MutableSet<String>? {
        val channel = osuSocket.manager.getChannel(channelName)

        if (channel != null) { return channel.user }
        else { return null }
    }
}
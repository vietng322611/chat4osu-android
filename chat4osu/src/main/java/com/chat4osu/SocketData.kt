package com.chat4osu

import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import osuIRC.Backend.OsuSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

        fun collect(): String {
            return try {
                osuSocket.stackTrace.last()
            } catch (e: NoSuchElementException) {
                ""
            }
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

        fun getMessage(channelName: String): MutableList<String> {
            osuSocket.manager.getChannel(channelName)?.let {
                return it.message
            }

            return mutableStateListOf()
        }

        fun saveMsg(msgList: List<String>) {
            osuSocket.manager.getChannel("")?.saveMsg(msgList)
        }

        fun getUser(channelName: String): List<String> {
            osuSocket.manager.getChannel(channelName)?.let {
                return it.user.toList()
            }
            return mutableListOf()
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

        fun getActiveChatType(): String {
            osuSocket.manager.getChannel("")?.let {
                return it.getType()
            }
            return ""
        }

        fun getAllChat(): List<String> {
            return osuSocket.manager.allChat
        }

        fun setActiveChat(name: String) {
            osuSocket.manager.setActiveChat(name)
        }

        fun archiveChat(name: String): String {
            val folder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(folder, "$name.log")

            val data = getMessage("")
            var fos: FileOutputStream? = null

            try {
                fos = FileOutputStream(file)

                for (msg in data)
                    fos.write(msg.toByteArray())

                fos.flush()
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            osuSocket.manager.removeChat(name)
            return "$folder/$name.log"
        }

        fun removeChat(name: String) {
            osuSocket.manager.removeChat(name)
        }
    }
}
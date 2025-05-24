package com.chat4osu.osuIRC

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.chat4osu.types.NoSuchChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

class OsuSocket {
    private val fatal = mutableStateOf(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    // Use atomic boolean for thread-safe flag
    private val pipeBroken = AtomicBoolean(false)
    private val sendMutex = Mutex()

    private lateinit var socket: Socket
    private lateinit var writer: BufferedWriter
    private lateinit var reader: BufferedReader

    private var retryCount = 0

    suspend fun connect(nick: String, pass: String): Int {
        retryCount++
        try {
            socket = Socket("irc.ppy.sh", 6667)
            writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        } catch (e: SocketTimeoutException) {
            delay(3000)
            if (retryCount > 10) {
                Log.e("OsuSocket", "connect: Connection timed out " + e.message)
                return 2
            }
            return connect(nick, pass)
        } catch (e: IOException) {
            Log.e("OsuSocket", "connect: " + e.message)
            return 3
        }

        retryCount = 0
        try {
            send("PASS $pass")
            send("NICK $nick")

            while(true) {
                val response = reader.readLine()
                Log.d("OsuSocket", "connect: $response")
                if (response == null) {
                    Log.d("OsuSocket", "connect: Connection closed")
                    return 3
                }

                val data = response.split(" ")
                var authenticated = false
                when (data[1]) {
                    "464" -> {
                        Log.e("OsuSocket", "connect: Wrong password or username")
                        return 1
                    }

                    "376" -> authenticated = true
                }
                if (authenticated) break
            }

            Manager.nick = nick
            Manager.pass = pass
            Manager.addChat("BanchoBot")

            pipeBroken.set(false)
            recv()
            keepAlive()

            return 0
        } catch (e: IOException) {
            Log.e("OsuSocket", "connect: ${e.message ?: "Unknown error"}")
            return 3
        }
    }

    suspend fun logout() {
        try {
            send("QUIT")
            socket.close()
        } catch (e: IOException) {
            Log.e("OsuSocket", "logout: " + e.message)
        }
    }

    private suspend fun reconnect() {
        val code = connect(Manager.nick, Manager.pass)
        for (chat in Manager.chatList) {
            if (!chat.startsWith("#")) continue
            join(chat)
        }
        if (code != 0) fatal.value = true
    }

    private fun keepAlive() = scope.launch {
        while (!socket.isClosed || !pipeBroken.get()) {
            try {
                send("KEEP_ALIVE")
                delay(30000)
            } catch (e: Exception) {
                Log.e("OsuSocket", "keepAlive: ${e.message}")
                break
            }
        }
    }

    private fun recv() = scope.launch {
        while (!pipeBroken.get()) {
            try {
                val msg = reader.readLine()
                if (msg == null) {
                    delay(50)
                    continue
                }
//              if (!msg.contains("QUIT")) Log.d("OsuSocket", "recv: $msg")
                if (msg == "PING cho.ppy.sh") send(msg)

                val parsedMessage = StringUtils.parse(msg)
                Manager.update(parsedMessage)
            } catch (e: NoSuchChannel) {
                Manager.removeChat("")
                Log.e("OsuSocket", "recv: " + e.message)
            } catch (e: Exception) {
                Log.e("OsuSocket", "recv: " + e.message)
                pipeBroken.set(true)
            }
        }
        reconnect()
    }

    suspend fun send(message: String) = sendMutex.withLock {
        try {
            writer.apply {
                write("$message\n")
                flush()
            }
            Log.d("OsuSocket", "send: $message")
        } catch (e: IOException) {
            Log.e("OsuSocket", "send: " + e.message)
            pipeBroken.set(true)
        }
    }

    suspend fun join(name: String) {
        if (Manager.getChat(name) == null) {
            Manager.addChat(name)
            send("JOIN $name")
        }
        Manager.activeChat = name
    }

    suspend fun part(name: String) {
        if (Manager.getChat(name) != null) send("PART $name")
        Manager.removeChat(name)
    }
}
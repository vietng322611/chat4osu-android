package com.chat4osu.osuIRC

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.chat4osu.osuIRC.global.Manager
import com.chat4osu.types.NoSuchChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketTimeoutException

class OsuSocket {
    private val fatal = mutableStateOf(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    private lateinit var socket: Socket
    private lateinit var writer: BufferedWriter
    private lateinit var reader: BufferedReader

    private var retryCount = 0
    private var pipeBroken = false

    @Throws(InterruptedException::class)
    fun connect(nick: String, pass: String): Int {
        retryCount++
        try {
            socket = Socket("irc.ppy.sh", 6667)
            writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        } catch (e: SocketTimeoutException) {
            Thread.sleep(3000)
            if (retryCount > 10) {
                Log.e("OsuSocket", "connect: Connection timed out")
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

            if (!pipeBroken) {
                recv()
                keepAlive()
            }
            Manager.addChat("BanchoBot")

            return 0
        } catch (e: IOException) {
            if (e.message != null) {
                Log.e("OsuSocket", "connect: " + e.message)
            } else {
                Log.e("OsuSocket", "connect: Unknown error")
            }
            return 3
        }
    }

    fun logout() {
        try {
            send("QUIT")
            socket.close()
        } catch (e: IOException) {
            Log.e("OsuSocket", "logout: " + e.message)
        }
    }

    private fun reconnect() {
        scope.launch {
            val code = connect(Manager.nick, Manager.pass)
            if (code != 0) fatal.value = true
        }
    }

    private fun keepAlive() {
        scope.launch {
            while (true) {
                if (!socket.isClosed) send("KEEP_ALIVE")
                delay(30000)
            }
        }
    }

    private fun recv() {
        scope.launch {
            while (reader.ready() || !pipeBroken) {
                try {
                    val msg = reader.readLine()
                    if (msg == null){
                        delay(50)
                        continue
                    }
//                    if (!msg.contains("QUIT")) Log.d("OsuSocket", "recv: $msg")
                    if (msg == "PING cho.ppy.sh") send(msg)

                    val parsedMessage = StringUtils.parse(msg)
                    Manager.update(parsedMessage)
                } catch (e: IOException) {
                    Log.e("OsuSocket", "recv: " + e.message)
                    pipeBroken = true
                } catch (e: NoSuchChannel) {
                    Manager.removeChat("")
                    Log.e("OsuSocket", "recv: " + e.message)
                }

                delay(50)
            }
            reconnect()
        }
    }

    fun send(message: String) = runBlocking {
        val job = launch(Dispatchers.IO) {
            try {
                writer.write("$message\n")
                writer.flush()
                Log.d("OsuSocket", "send: $message")
            } catch (e: IOException) {
                Log.e("OsuSocket", "send: " + e.message)
                pipeBroken = true
            }
        }
        job.join()
    }

    fun join(name: String) {
        if (Manager.getChat(name) == null) send("JOIN $name")
        Manager.activeChat = name
    }

    fun part(name: String) {
        if (Manager.getChat(name) != null) {
            send("PART $name")
            Manager.removeChat(name)
        }
    }
}
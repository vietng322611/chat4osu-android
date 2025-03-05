package osuIRC

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    val manager = Manager()
    private lateinit var socket: Socket
    private lateinit var writer: BufferedWriter
    private lateinit var reader: BufferedReader
    private val mQueue = Channel<String>(capacity = Channel.UNLIMITED)

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
            putMessage("PASS $pass")
            putMessage("NICK $nick")

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

            manager.nick = nick
            manager.pass = pass

            if (!pipeBroken) {
                recv()
                keepAlive()
            }

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

    private fun reconnect() {
        scope.launch {
            val code = connect(manager.nick, manager.pass)
            if (code != 0) fatal.value = true
        }
    }

    private fun keepAlive() {
        scope.launch {
            while (true) {
                if (!socket.isClosed) putMessage("KEEP_ALIVE")
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
                    if (msg == "PING cho.ppy.sh") putMessage(msg)

                    val parsedMessage = StringUtils.parse(msg)
                    manager.update(parsedMessage)
                } catch (e: IOException) {
                    Log.e("OsuSocket", "recv: " + e.message)
                    pipeBroken = true
                } catch (e: NoSuchChannel) {
                    manager.removeChat("")
                    Log.e("OsuSocket", "recv: " + e.message)
                }

                delay(50)
            }
            reconnect()
        }
    }

    fun putMessage(message: String) {
        scope.launch {
            mQueue.send(message)
        }
    }

    fun send() {
        scope.launch {
            for (message in mQueue) {
                try {
                    writer.write("$message\n")
                    writer.flush()
                    Log.d("OsuSocket", "send: $message")
                } catch (e: IOException) {
                    Log.e("OsuSocket", "send: " + e.message)
                    pipeBroken = true
                    break
                }
            }
        }
    }

    fun join(name: String) {
        if (manager.getChannel(name) == null) putMessage("JOIN $name")
        manager.activeChat = name
    }

    fun part(name: String) {
        if (manager.getChannel(name) != null) {
            putMessage("PART $name")
            manager.removeChat(name)
        }
    }

    fun archiveChat(name: String): String? {
        val channel = manager.getChannel(name)
        if (channel == null) return channel

        part(name)
        val outputFile = channel.archiveChat()
        if (outputFile != null) {
            manager.removeChat(name)
        }

        return outputFile
    }
}
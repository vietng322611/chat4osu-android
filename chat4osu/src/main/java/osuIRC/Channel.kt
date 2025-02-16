package osuIRC

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class Channel(var name: String) {
    private val lock: Lock = ReentrantLock()

    var type = ""
    private val userList = ConcurrentSkipListSet<String>()
    private val messageList = mutableListOf<String>()
    private val messageOnStage = mutableListOf<String>()

    private val patterns = listOf(
        Regex("BanchoBot : (.*) joined in slot \\d.") to { match: String -> addUser(listOf(match)) },
        Regex("BanchoBot : (.*) left the game.") to { match: String -> removeUser(match) }
    )

    val getUser: Set<String>
        get() = userList

    val getType: String
        get() = type

    fun update(data: List<String?>) {
        val message = String.format(
            "[%s] %s: %s",
            currentTimeStamp, data[2], data[3]
        )

        updateMessage(message)

        for ((pattern, action) in patterns) {
            if (!pattern.matches(message)) continue

            val match = pattern.find(message)
            if (match != null) {
                action(match.groupValues[1])
                return
            }
        }
    }

    private fun updateMessage(message: String) {
        lock.lock()
        try {
            messageList.add(message)
            messageOnStage.add(message)
        } finally {
            lock.unlock()
        }
    }

    fun addUser(name: List<String>) {
        userList.addAll(name)
    }

    fun removeUser(name: String) {
        userList.remove(name)
    }

    fun pullMessage(): List<String> {
        val ret = messageOnStage.toMutableList()
        messageOnStage.clear()
        return ret
    }

    fun archiveChat(): String? {
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(folder, "$name.log")
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            val content = messageList.joinToString("\n")
            Log.d("Info", content)
            fos.write(content.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            Log.d("Error", "archiveChat: ${e.message}")
            return null
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    Log.d("error", "archiveChat: Error closing output stream")
                }
            }
        }

        return "$folder/$name.log"
    }

    companion object {
        val currentTimeStamp: String
            get() {
                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault()) //dd/MM/yyyy
                val now = Date()
                return formatter.format(now)
            }
    }
}
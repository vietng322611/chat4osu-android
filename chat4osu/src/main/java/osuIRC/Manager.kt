package osuIRC

import android.util.Log

class Manager {
    private val channelList: MutableMap<String, Channel?> = HashMap()

    var nick = ""
    var pass = ""
    var activeChat = ""

    fun addChat(name: String) {
        if (!channelList.containsKey(name)) {
            channelList[name] = Channel(name)
            channelList[name]?.type = if (name.startsWith("#mp_")) {
                    "lobby"
                } else if (name.startsWith("#")) {
                    "chat"
                } else {
                    "DM"
                }
        }
    }

    val allChat: List<String>
        get() = ArrayList(channelList.keys)

    fun removeChat(name: String) {
        channelList.remove(name)
    }

    fun update(data: List<String>) {
        if (data.isEmpty()) return

        var name = data[1]
        try {
            when (data[0]) {
                "0" -> { // join
                    if (data[2] == nick) addChat(name)
                    channelList[name]?.addUser(data.subList(2, data.size))
                }

                "1" -> { // message
                    if (name == nick) name = data[2]
                    if (!channelList.containsKey(name)) addChat(name)
                    channelList[name]?.update(data)
                }

                "2" -> { // leave
                    if (data[2] == nick) removeChat(name)
                    else channelList[name]?.removeUser(data[2])
                }
            }
        }
        catch (e: Exception) {
            Log.d("Error", e.message ?: "Unknown error")
        }
    }

    fun getChannel(name: String): Channel? {
        return if (name.isEmpty())
            channelList[activeChat]
        else
            channelList[name]
    }
}
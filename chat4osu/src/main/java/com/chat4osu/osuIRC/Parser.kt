package com.chat4osu.osuIRC

/*
* :cho.ppy.sh 353 [nick] = [channel_name] :[name_list]
* :[name]!cho@ppy.sh PRIVMSG [channel_name] :[message]
* :cho.ppy.sh 403 [nick] [channel_name] :No such channel
*/
object StringUtils {
    @Throws(NoSuchChannel::class)
    fun parse(message: String): List<String> {
        val data = message.split(":")
        val retData: MutableList<String> = ArrayList()

        val mData = data[1].split(" ")

        if (mData[1] == "QUIT") return retData

        when (mData[1]) {
            "401", "403" -> throw NoSuchChannel(mData[3])
            "323", "353" -> {
                // indicator, channel, username
                retData.add("0")
                retData.add(mData[4])
                retData.addAll(data[2].split(" "))
            }

            "JOIN" -> {
                // indicator, channel, username
                retData.add("0")
                retData.add(data[2])
                retData.add(mData[0].split("!")[0])
            }

            "PRIVMSG" -> {
                // indicator, channel, username, message
                retData.add("1")
                retData.add(mData[2])
                retData.add(mData[0].split("!")[0])
                retData.add(
                    data.subList(2, data.size)
                        .joinToString(":")
                )
            }

            "PART" -> {
                // indicator, channel, username
                retData.add("2")
                retData.add(data[2])
                retData.add(mData[0].split("!")[0])
            }
        }
        return retData
    }
}
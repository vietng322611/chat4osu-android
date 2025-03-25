package com.chat4osu.types

import java.util.regex.Pattern

class MatchData(id: Int) {
    var matchID = id
    var lobbyName = ""
    var matchRules = ""
    var redTeam = ""
    var blueTeam = ""
    val redPlayers = mutableListOf<String>()
    val bluePlayers = mutableListOf<String>()
    var redScore = 0
    var blueScore = 0
    val protects = mutableListOf<String>()
    val bans = mutableListOf<String>()
    val picks = mutableListOf<String>()
    val commands = mutableMapOf<String, String>().withDefault { "" }

    /*
    * matchRules:...
    * redTeam:...
    * blueTeam:...
    * redPlayers:a,b,c,d,...
    * bluePlayers:a,b,c,d,...
    * NM1:!mp mods 1,!mp map xxxxxx
    * NM2:...
    * ...
    * */

    fun parseMatchData(data: List<String>) {
        for (line in data) {
            val splitLine = line.split(":")
            val prop = splitLine[0]
            val value = splitLine[1]
            when (prop) {
                "matchRules" -> matchRules = value
                "redTeam" -> redTeam = value
                "blueTeam" -> blueTeam = value
                "redPlayers" -> redPlayers.addAll(value.split(","))
                "bluePlayers" -> bluePlayers.addAll(value.split(","))
                else -> {
                    val pattern = Pattern.compile("^(NM|HD|HR|DT|FM|TB)(\\d?)")
                    val matcher = pattern.matcher(prop)
                    if (matcher.find()) {
                        val command = matcher.group(1)
                        val num = matcher.group(2)
                        commands["$command$num"] = value
                    }
                }
            }
        }
    }
}
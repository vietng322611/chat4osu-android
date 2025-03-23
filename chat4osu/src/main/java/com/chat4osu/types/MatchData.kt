package com.chat4osu.types

class MatchData {
    var matchID = 0
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
}
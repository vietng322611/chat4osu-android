package com.chat4osu.di

import com.chat4osu.types.MatchData

class MatchManager {
    companion object {
        private val manager = mutableMapOf<Int, MatchData>().withDefault { MatchData() }

        fun addMatch(matchID: Int, data: List<String>): MatchData {
            val newMatch = MatchData()
            for (line in data) {
                val splitLine = line.split(":")
                when (splitLine[0]) {
                    "MatchID" -> newMatch.matchID = matchID
                    "LobbyName" -> newMatch.lobbyName = splitLine[1]
                    "MatchRules" -> newMatch.matchRules = splitLine[1]
                    "RedTeam" -> newMatch.redTeam = splitLine[1]
                    "BlueTeam" -> newMatch.blueTeam = splitLine[1]
                    "RedPlayers" -> newMatch.redPlayers.addAll(splitLine[1].split(","))
                    "BluePlayers" -> newMatch.bluePlayers.addAll(splitLine[1].split(","))
                    else -> newMatch.commands[splitLine[0]] = splitLine[1]
                }
            }
            manager[matchID] = newMatch
            return newMatch
        }

        fun getMatch(matchID: Int): MatchData {
            return manager.getValue(matchID)
        }
    }
}
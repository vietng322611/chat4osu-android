package com.chat4osu.types

import com.chat4osu.utils.Utils

class MatchData(id: Int) {
    private var _matchID = id
    private var _matchRules = ""
    private var _redTeam = ""
    private var _blueTeam = ""
    private val _redPlayers = mutableListOf<String>()
    private val _bluePlayers = mutableListOf<String>()
    private var _redScore = 0
    private var _blueScore = 0
    private val _protects = mutableListOf<String>()
    private val _bans = mutableListOf<String>()
    private val _redPicks = mutableListOf<String>()
    private val _bluePicks = mutableListOf<String>()
    private val _picks = mutableMapOf<String, List<String>>()

    val matchRules: String
        get() = _matchRules

    var redScore: Int
        get() = _redScore
        set(value) { _redScore += value }

    var blueScore: Int
        get() = _redScore
        set(value) { _blueScore += value }

    fun getPick(pick: String): List<String>? {
        return _picks[pick]
    }

    private val patterns = listOf(
        Regex("^(MatchRules):(!mp set \\d \\d \\d(\\d?))"),
        Regex("^(RedTeam):([\\w\\s]+)"),
        Regex("^(BlueTeam):([\\w\\s]+)"),
        Regex("^(RedPlayers):([\\w,]+)"),
        Regex("^(BluePlayers):([\\w,]+)"),
        Regex("^(NM|HD|HR|DT|FM|TB)(\\d?):!mp mods (\\d+(\\s\\w+)?),!mp map (\\d+) (\\d)"),
    )

//    MatchRules:!mp set 2 3 11
//    RedTeam:bb
//    BlueTeam:aa
//    RedPlayers:aa,bb,cc
//    BluePlayers:aa,bb,cc
//    NM1:!mp mods 1,!mp map 4874071 0

    fun parseMatchData(data: List<String>): Int {
        for (line in data) {
            for (pattern in patterns) {
                val match = pattern.find(line) ?: continue
                val groups = match.groupValues.drop(1)
                when (groups[0]) {
                    "MatchRules" -> _matchRules = groups[1]
                    "RedTeam" -> _redTeam = groups[1]
                    "BlueTeam" -> _blueTeam = groups[1]
                    "RedPlayers" -> _redPlayers.addAll(groups[1].split(","))
                    "BluePlayers" -> _bluePlayers.addAll(groups[1].split(","))
                    else -> {
                        val pick = groups[0] + groups[1]
                        _picks[pick] = listOf(groups[2], groups[4], groups[5])
                    }
                }
            }
        }
        return 0
    }

    fun exportMatchData(): Map<String, String> {
        var maps = ""
        // NM1,[id]
        // NM2,[id]
        for ((k, v) in _picks)
            maps += "$k,${v[1]}\n"
        return mapOf(
            "Match ID" to _matchID.toString(),
            "Match rules" to _matchRules,
            "Red team" to _redTeam,
            "Blue team" to _blueTeam,
            "Red players" to _redPlayers.joinToString(","),
            "Blue players" to _bluePlayers.joinToString(","),
            "Red score" to _redScore.toString(),
            "Blue score" to _blueScore.toString(),
            "Protects" to _protects.joinToString(","),
            "Bans" to _bans.joinToString(","),
            "Red picks" to _redPicks.joinToString(","),
            "Blue picks" to _bluePicks.joinToString(","),
            "Maps" to maps.removeSuffix("\n"),
        )
    }

    fun archiveMatch(): String? {
        val dataExport = exportMatchData()
        var content = ""
        for ((key, value) in dataExport)
            content += "$key: $value\n"

        return Utils.saveFile("match_data_${_matchID}", content, "archiveMatch")
    }
}
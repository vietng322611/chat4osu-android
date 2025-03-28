package com.chat4osu.types

class MatchData(id: Int) {
    private var _matchID = id
    private var _lobbyName = ""
    private var _matchRules = ""
    private var _redTeam = ""
    private var _blueTeam = ""
    private val _redPlayers = mutableListOf<String>()
    private val _bluePlayers = mutableListOf<String>()
    private var _redScore = 0
    private var _blueScore = 0
    private val _protects = mutableListOf<String>()
    private val _bans = mutableListOf<String>()
    private val _playerPicks = mutableListOf<String>()
    private val _picks = mutableMapOf<String, List<String>>()

    val matchRules: String
        get() = _matchRules

    val redTeam: String
        get() = _redTeam

    val blueTeam: String
        get() = _blueTeam

    var redScore: Int
        get() = _redScore
        set(value) { _redScore += value }

    var blueScore: Int
        get() = _redScore
        set(value) { _blueScore += value }

    val protects: List<String>
        get() = _protects

    val bans: List<String>
        get() = _bans

    val playerPicks: List<String>
        get() = _playerPicks

    val picks: Map<String, List<String>>
        get() = _picks

    fun getPick(pick: String): List<String>? {
        return _picks[pick]
    }

    private val patterns = listOf(
        Regex("^(MatchRules):(!mp set \\d \\d \\d(\\d?))"),
        Regex("^(RedTeam):([\\w\\s]+)"),
        Regex("^(BlueTeam):([\\w\\s]+)"),
        Regex("^(RedPlayers):([\\w,]+)"),
        Regex("^(BluePlayers):([\\w,]+)"),
        Regex("^(NM|HD|HR|DT|FM|TB)(\\d?):(!mp mods \\d(\\d?),!mp map (\\d+))"),
    )

    /*
    * MatchRules:!mp set 2 3 11
    * RedTeam:bb
    * BlueTeam:aa
    * RedPlayers:aa,bb,cc,...
    * BluePlayers:aa,bb,cc,...
    * NM1:!mp mods 1,!mp map 4874071 0
    * */

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
                        _picks[pick] = groups[2].split(",")
                    }
                }
            }
        }
        return 0
    }
}
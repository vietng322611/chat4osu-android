package com.chat4osu.services

import android.util.Log
import com.chat4osu.osuIRC.Manager
import com.chat4osu.types.MatchData
import com.chat4osu.utils.Utils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchService @Inject constructor(
    private val chatService: ChatService
) {
    private val patterns = listOf(
        Regex("/set") to { setMatchRule() },
        Regex("/pick (\\w{3})") to  { pick: String -> pickMap(pick) },
        Regex("/pick (\\d+)") to { pick: Int -> pickMap(pick) },
        Regex("/timer\\s?(\\d+)?\\s?([\\w\\s]+)?") to
                { time: Int?, text: String? ->
                    val actualTime = time ?: 90
                    val actualText = text ?: ""
                    setTimer(actualTime, actualText)
                },
        Regex("/start\\s?(\\d+)?\\s?([\\w\\s]+)?") to
                { time: Int?, text: String? ->
                    val actualTime = time ?: 90
                    val actualText = text ?: ""
                    startMatch(actualTime, actualText)
                },
        Regex("/score (\\w)") to  { team: Char -> setScore(team) },
        Regex("/unscore (\\w)") to  { team: Char -> undoScore(team) },
    )

    fun readInput(input: String) {
        for ((pattern, action) in patterns) {
            pattern.find(input)?.let { match ->
                Utils.handleAction(action, match.groupValues.drop(1))
                return
            }
        }
    }

    fun parseMatchData(data: List<String>): Int {
        return Manager.parseMatchData(data)
    }

    fun saveMatchData(): String? {
        return Manager.archiveMatch()
    }

    // Nah bro this shouldn't be callable if there is no match data
    fun exportMatchData(): Map<String, String> {
        val match = getMatch()
        return match!!.exportMatchData()
    }

    private fun getMatch(): MatchData? {
        val match = Manager.getMatch()
        if (match == null) {
            chatService.updateMessage("Failed to get match data")
            Log.d("getMatch", "Lobby ${Manager.activeChat} has no match data")
            return null
        }
        return match
    }

    private fun setMatchRule() {
        val match = getMatch() ?: return
        if (match.matchRules.isEmpty())
            chatService.updateMessage("Match rules not found")
        else
            chatService.send(match.matchRules)
    }

    private fun pickMap(pick: String) {
        val match = getMatch() ?: return
        val commands = match.getPick(pick.uppercase())
        if (commands == null) {
            chatService.updateMessage("Pick $pick not found")
            return
        }
        for (command in commands)
            chatService.send(command)
    }

    private fun pickMap(pick: Int) {
        chatService.send("!mp map $pick")
    }

    private fun setTimer(time: Int, text: String) {
        chatService.send("!mp timer $time $text")
    }

    private fun startMatch(time: Int, text: String) {
        chatService.send("!mp start $time $text")
    }

    private fun setScore(team: Char) {
        val match = getMatch() ?: return
        if (team == 'r') match.redScore++ // increase by 1
        else match.blueScore++
    }

    private fun undoScore(team: Char) {
        val match = getMatch() ?: return
        if (team == 'r') match.redScore-- // decrease by 1
        else match.blueScore--
    }
}
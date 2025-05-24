package com.chat4osu.viewmodel

import androidx.lifecycle.ViewModel
import com.chat4osu.services.MatchService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MatchConfigViewModel @Inject constructor(
    private val matchService: MatchService
): ViewModel() {
    fun getMatchConfig() = matchService.exportMatchData()

    fun saveConfig(matchData: Map<String, String>) {
        val data = mutableListOf<String>()
        matchData.forEach{ (key, value) ->
            data.add("$key:$value")
        }
        matchService.parseMatchData(data)
    }
}
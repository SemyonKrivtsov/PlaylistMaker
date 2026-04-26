package com.example.playlistmaker.helpers

import android.content.SharedPreferences
import com.example.playlistmaker.model.Track
import com.google.gson.Gson

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private var cachedHistory: MutableList<Track> = readFromPreferences().toMutableList()

    fun add(track: Track) {
        val index = cachedHistory.indexOfFirst { it.trackId == track.trackId }

        if (index != -1) {
            cachedHistory.removeAt(index)
        }

        cachedHistory.add(0, track)

        if (cachedHistory.size > TRACK_HISTORY_SIZE) {
            cachedHistory.removeAt(TRACK_HISTORY_SIZE)
        }

        writeToPreferences()
    }

    fun getHistory(): List<Track> {
        return cachedHistory
    }

    fun clear() {
        cachedHistory.clear()
        sharedPreferences.edit()
            .remove(TRACK_HISTORY)
            .apply()
    }

    private fun readFromPreferences(): Array<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY, null) ?: return emptyArray()
        return gson.fromJson(json, Array<Track>::class.java)
    }

    private fun writeToPreferences() {
        val json = gson.toJson(cachedHistory)
        sharedPreferences.edit()
            .putString(TRACK_HISTORY, json)
            .apply()
    }

    companion object {
        private const val TRACK_HISTORY = "tracks_history_list"
        private const val TRACK_HISTORY_SIZE = 10
        private val gson = Gson()
    }
}

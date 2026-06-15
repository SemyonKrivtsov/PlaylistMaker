package com.example.playlistmaker.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.domain.api.SearchHistoryRepository
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SearchHistoryRepository {

    private val gson = Gson()
    private var cachedHistory: MutableList<Track> = readFromPreferences().toMutableList()

    override fun add(track: Track) {
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

    override fun getHistory(): List<Track> = cachedHistory

    override fun clear() {
        cachedHistory.clear()
        sharedPreferences.edit {
            remove(TRACK_HISTORY)
        }
    }

    private fun readFromPreferences(): Array<Track> {
        val json = sharedPreferences.getString(TRACK_HISTORY, null) ?: return emptyArray()
        return gson.fromJson(json, Array<Track>::class.java)
    }

    private fun writeToPreferences() {
        val json = gson.toJson(cachedHistory)
        sharedPreferences.edit {
            putString(TRACK_HISTORY, json)
        }
    }

    companion object {
        private const val TRACK_HISTORY = "tracks_history_list"
        private const val TRACK_HISTORY_SIZE = 10
    }
}

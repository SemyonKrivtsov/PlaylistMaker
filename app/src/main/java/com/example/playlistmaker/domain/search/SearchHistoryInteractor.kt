package com.example.playlistmaker.domain.search

import com.example.playlistmaker.domain.search.model.Track

interface SearchHistoryInteractor {
    fun add(track: Track)
    fun getHistory(): List<Track>
    fun clear()
}

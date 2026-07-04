package com.example.playlistmaker.domain.search

import com.example.playlistmaker.domain.search.model.Track

interface SearchHistoryRepository {
    fun add(track: Track)
    fun getHistory(): List<Track>
    fun clear()
}

package com.example.playlistmaker.domain.api

import com.example.playlistmaker.domain.models.Track

interface SearchHistoryInteractor {
    fun add(track: Track)
    fun getHistory(): List<Track>
    fun clear()
}

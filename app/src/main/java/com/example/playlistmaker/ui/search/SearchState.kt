package com.example.playlistmaker.ui.search

import com.example.playlistmaker.domain.search.model.Track

sealed interface SearchState {
    data object Loading : SearchState
    data class Content(val tracks: List<Track>) : SearchState
    data class History(val tracks: List<Track>) : SearchState
    data object NothingFound : SearchState
    data object ConnectionError : SearchState
    data object Empty : SearchState
}

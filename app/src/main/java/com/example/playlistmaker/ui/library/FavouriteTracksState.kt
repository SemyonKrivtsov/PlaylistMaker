package com.example.playlistmaker.ui.library

import com.example.playlistmaker.domain.search.model.Track

sealed interface FavouriteTracksState {
    data object Empty : FavouriteTracksState
    data class Content(val tracks: List<Track>) : FavouriteTracksState
}
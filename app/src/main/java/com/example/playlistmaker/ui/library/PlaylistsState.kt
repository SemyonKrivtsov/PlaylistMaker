package com.example.playlistmaker.ui.library

import com.example.playlistmaker.domain.library.model.Playlist

sealed interface PlaylistsState {
    data object Empty : PlaylistsState
    data class Content(val playlists: List<Playlist>) : PlaylistsState
}

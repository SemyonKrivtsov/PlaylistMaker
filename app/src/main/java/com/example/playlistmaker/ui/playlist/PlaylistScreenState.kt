package com.example.playlistmaker.ui.playlist

import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.domain.search.model.Track

data class PlaylistScreenState(
    val playlist: Playlist,
    val tracks: List<Track>,
    val durationMinutes: Int
)

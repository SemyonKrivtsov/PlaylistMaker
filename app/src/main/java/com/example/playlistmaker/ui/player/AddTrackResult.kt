package com.example.playlistmaker.ui.player

sealed interface AddTrackResult {
    data class Added(val playlistTitle: String) : AddTrackResult
    data class AlreadyAdded(val playlistTitle: String) : AddTrackResult
}

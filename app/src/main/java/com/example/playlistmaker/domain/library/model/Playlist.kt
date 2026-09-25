package com.example.playlistmaker.domain.library.model

data class Playlist(
    val id: Long = 0,
    val title: String,
    val description: String?,
    val coverPath: String?,
    val trackIds: List<Long>
)
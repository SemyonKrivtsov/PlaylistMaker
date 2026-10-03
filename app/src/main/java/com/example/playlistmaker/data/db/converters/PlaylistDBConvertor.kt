package com.example.playlistmaker.data.db.converters

import com.example.playlistmaker.data.db.entity.PlaylistEntity
import com.example.playlistmaker.data.db.entity.PlaylistWithTracks
import com.example.playlistmaker.domain.library.model.Playlist

class PlaylistDBConvertor {
    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id,
            title = playlist.title,
            description = playlist.description,
            coverPath = playlist.coverPath
        )
    }

    fun map(playlistWithTracks: PlaylistWithTracks): Playlist {
        val entity = playlistWithTracks.playlist
        return Playlist(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            coverPath = entity.coverPath,
            trackIds = playlistWithTracks.tracks.map { it.trackId }
        )
    }
}

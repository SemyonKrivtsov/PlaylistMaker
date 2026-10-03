package com.example.playlistmaker.domain.library

import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {
    suspend fun createPlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(playlistId: Long)

    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(playlistId: Long): Flow<Playlist?>
    fun getPlaylistTracks(playlistId: Long): Flow<List<Track>>

    suspend fun addTrackToPlaylist(track: Track, playlist: Playlist)
    suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long)
}

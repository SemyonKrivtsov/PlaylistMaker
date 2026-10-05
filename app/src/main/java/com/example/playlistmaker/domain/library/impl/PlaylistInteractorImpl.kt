package com.example.playlistmaker.domain.library.impl

import com.example.playlistmaker.domain.library.PlaylistInteractor
import com.example.playlistmaker.domain.library.PlaylistRepository
import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(private val repository: PlaylistRepository) : PlaylistInteractor {
    override suspend fun createPlaylist(playlist: Playlist) {
        repository.createPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        repository.deletePlaylist(playlistId)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return repository.getPlaylists()
    }

    override fun getPlaylistById(playlistId: Long): Flow<Playlist?> {
        return repository.getPlaylistById(playlistId)
    }

    override fun getPlaylistTracks(playlistId: Long): Flow<List<Track>> {
        return repository.getPlaylistTracks(playlistId)
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        repository.addTrackToPlaylist(track, playlist)
    }

    override suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long) {
        repository.deleteTrackFromPlaylist(playlistId, trackId)
    }
}

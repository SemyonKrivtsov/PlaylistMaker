package com.example.playlistmaker.data.library

import com.example.playlistmaker.data.db.converters.PlaylistDBConvertor
import com.example.playlistmaker.data.db.converters.PlaylistTrackDBConvertor
import com.example.playlistmaker.data.db.dao.PlaylistDao
import com.example.playlistmaker.data.db.dao.PlaylistTrackDao
import com.example.playlistmaker.data.storage.ImageStorage
import com.example.playlistmaker.domain.library.PlaylistRepository
import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistDbConvertor: PlaylistDBConvertor,
    private val playlistTrackDao: PlaylistTrackDao,
    private val playlistTrackDbConvertor: PlaylistTrackDBConvertor,
    private val imageStorage: ImageStorage
) : PlaylistRepository {
    override suspend fun createPlaylist(playlist: Playlist) {
        val coverPath = playlist.coverPath?.let { imageStorage.saveImageToPrivateStorage(it) }
        val playlistEntity = playlistDbConvertor.map(playlist.copy(coverPath = coverPath))
        playlistDao.insertPlaylist(playlistEntity)
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        val updatedPlaylist = playlist.copy(trackIds = playlist.trackIds + track.trackId)
        playlistDao.updatePlaylist(playlistDbConvertor.map(updatedPlaylist))
        playlistTrackDao.insertPlaylistTrack(
            playlistTrackDbConvertor.map(track, System.currentTimeMillis())
        )
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylists()
            .distinctUntilChanged()
            .map { entities -> entities.map { playlistDbConvertor.map(it) } }
    }
}
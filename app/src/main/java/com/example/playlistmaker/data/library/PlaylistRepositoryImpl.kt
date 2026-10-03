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
        playlistTrackDao.addTrackToPlaylist(
            track = playlistTrackDbConvertor.map(track, System.currentTimeMillis()),
            playlistId = playlist.id
        )
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylistsWithTracks()
            .distinctUntilChanged()
            .map { playlists -> playlists.map { playlistDbConvertor.map(it) } }
    }
}
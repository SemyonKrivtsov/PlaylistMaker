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
import kotlinx.coroutines.flow.first
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

    override suspend fun updatePlaylist(playlist: Playlist) {
        val oldCoverPath = playlistDao.getPlaylistWithTracksById(playlist.id).first()
            ?.playlist?.coverPath
        val coverPath = if (playlist.coverPath != null && playlist.coverPath != oldCoverPath) {
            imageStorage.saveImageToPrivateStorage(playlist.coverPath) ?: oldCoverPath
        } else {
            oldCoverPath
        }
        playlistDao.updatePlaylist(playlistDbConvertor.map(playlist.copy(coverPath = coverPath)))
        if (oldCoverPath != null && oldCoverPath != coverPath) {
            imageStorage.deleteImage(oldCoverPath)
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val playlist = playlistDao.getPlaylistWithTracksById(playlistId).first() ?: return
        playlistDao.deletePlaylist(playlistId)
        playlist.tracks.forEach { deleteTrackIfUnused(it.trackId) }
        playlist.playlist.coverPath?.let { imageStorage.deleteImage(it) }
    }

    override suspend fun addTrackToPlaylist(track: Track, playlist: Playlist) {
        playlistTrackDao.addTrackToPlaylist(
            track = playlistTrackDbConvertor.map(track, System.currentTimeMillis()),
            playlistId = playlist.id
        )
    }

    override suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistTrackDao.deleteTrackFromPlaylist(playlistId, trackId)
        deleteTrackIfUnused(trackId)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getPlaylistsWithTracks()
            .distinctUntilChanged()
            .map { playlists -> playlists.map { playlistDbConvertor.map(it) } }
    }

    override fun getPlaylistById(playlistId: Long): Flow<Playlist?> {
        return playlistDao.getPlaylistWithTracksById(playlistId)
            .distinctUntilChanged()
            .map { playlist -> playlist?.let { playlistDbConvertor.map(it) } }
    }

    override fun getPlaylistTracks(playlistId: Long): Flow<List<Track>> {
        return playlistTrackDao.getPlaylistTracks(playlistId)
            .distinctUntilChanged()
            .map { tracks -> tracks.map { playlistTrackDbConvertor.map(it) } }
    }

    private suspend fun deleteTrackIfUnused(trackId: Long) {
        if (!playlistTrackDao.isTrackInAnyPlaylist(trackId)) {
            playlistTrackDao.deleteTrack(trackId)
        }
    }
}

package com.example.playlistmaker.data.library

import com.example.playlistmaker.data.db.converters.TrackDBConvertor
import com.example.playlistmaker.data.db.dao.TrackDao
import com.example.playlistmaker.domain.library.FavouriteTracksRepository
import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavouriteTracksRepositoryImpl(
    private val trackDao: TrackDao,
    private val trackDBConvertor: TrackDBConvertor
) : FavouriteTracksRepository {
    override suspend fun addToFavourites(track: Track) {
        val trackEntity = trackDBConvertor.map(track, System.currentTimeMillis())
        trackDao.insertTrack(trackEntity)
    }

    override suspend fun removeFromFavourites(track: Track) {
        val trackEntity = trackDBConvertor.map(track, System.currentTimeMillis())
        trackDao.deleteTrack(trackEntity)
    }

    override fun getFavouriteTracks(): Flow<List<Track>> =
        trackDao.getFavouriteTracks().map { entities -> entities.map { trackDBConvertor.map(it) } }

    override suspend fun isFavourite(trackId: Long): Boolean = trackDao.isFavourite(trackId)
}
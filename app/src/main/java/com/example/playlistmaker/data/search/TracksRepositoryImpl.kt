package com.example.playlistmaker.data.search

import com.example.playlistmaker.data.db.dao.TrackDao
import com.example.playlistmaker.data.search.dto.TracksSearchRequest
import com.example.playlistmaker.data.search.dto.TracksSearchResponse
import com.example.playlistmaker.domain.search.TracksRepository
import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TracksRepositoryImpl(
    private val networkClient: NetworkClient,
    private val trackDao: TrackDao
) : TracksRepository {

    override fun searchTracks(expression: String): Flow<List<Track>?> = flow {
        val response = networkClient.doRequest(TracksSearchRequest(expression))
        val tracks = if (response.resultCode == 200) {
            val favouriteIds = trackDao.getFavouriteTrackIds().toSet()

            (response as TracksSearchResponse).results.map { dto ->
                Track(
                    trackId = dto.trackId,
                    trackName = dto.trackName,
                    artistName = dto.artistName,
                    trackTimeMillis = dto.trackTimeMillis,
                    artworkUrl100 = dto.artworkUrl100,
                    collectionName = dto.collectionName,
                    releaseDate = dto.releaseDate,
                    primaryGenreName = dto.primaryGenreName,
                    country = dto.country,
                    previewUrl = dto.previewUrl,
                    isFavourite = favouriteIds.contains(dto.trackId)
                )
            }
        } else {
            null
        }
        emit(tracks)
    }
}

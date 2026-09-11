package com.example.playlistmaker.domain.library

import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.flow.Flow

interface FavouriteTracksInteractor {
    suspend fun addToFavourites(track: Track)
    suspend fun removeFromFavourites(track: Track)
    fun getFavouriteTracks(): Flow<List<Track>>
    suspend fun isFavourite(trackId: Long): Boolean
}
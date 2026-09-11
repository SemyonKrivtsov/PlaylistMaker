package com.example.playlistmaker.domain.library.impl

import com.example.playlistmaker.domain.library.FavouriteTracksInteractor
import com.example.playlistmaker.domain.library.FavouriteTracksRepository
import com.example.playlistmaker.domain.search.model.Track
import kotlinx.coroutines.flow.Flow

class FavouriteTracksInteractorImpl(
    private val repository: FavouriteTracksRepository
) : FavouriteTracksInteractor {

    override suspend fun addToFavourites(track: Track) =
        repository.addToFavourites(track)

    override suspend fun removeFromFavourites(track: Track) =
        repository.removeFromFavourites(track)

    override fun getFavouriteTracks(): Flow<List<Track>> =
        repository.getFavouriteTracks()

    override suspend fun isFavourite(trackId: Long): Boolean =
        repository.isFavourite(trackId)
}
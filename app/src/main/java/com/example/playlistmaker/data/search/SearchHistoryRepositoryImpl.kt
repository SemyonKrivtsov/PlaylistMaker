package com.example.playlistmaker.data.search

import com.example.playlistmaker.data.StorageClient
import com.example.playlistmaker.data.db.dao.TrackDao
import com.example.playlistmaker.domain.search.SearchHistoryRepository
import com.example.playlistmaker.domain.search.model.Track

class SearchHistoryRepositoryImpl(
    private val storage: StorageClient<ArrayList<Track>>,
    private val trackDao: TrackDao
) : SearchHistoryRepository {

    override fun add(track: Track) {
        val history = storage.getData() ?: arrayListOf()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        while (history.size > TRACK_HISTORY_SIZE) {
            history.removeAt(history.lastIndex)
        }
        storage.storeData(history)
    }

    override suspend fun getHistory(): List<Track> {
        val history = storage.getData() ?: return emptyList()
        val favouriteIds = trackDao.getFavouriteTrackIds().toSet()
        return history.map { it.copy(isFavourite = favouriteIds.contains(it.trackId)) }
    }

    override fun clear() {
        storage.storeData(arrayListOf())
    }

    companion object {
        private const val TRACK_HISTORY_SIZE = 10
    }
}

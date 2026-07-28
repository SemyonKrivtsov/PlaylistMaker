package com.example.playlistmaker.ui.search.view_model

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TracksInteractor
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.search.SearchState

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor,
    private val handler: Handler
) : ViewModel() {

    private val stateLiveData = MutableLiveData<SearchState>(SearchState.Empty)
    fun observeState(): LiveData<SearchState> = stateLiveData

    private var latestSearchText: String = ""
    private val searchRunnable = Runnable { searchRequest(latestSearchText) }

    fun searchDebounce(query: String) {
        latestSearchText = query
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    fun searchImmediately(query: String) {
        latestSearchText = query
        handler.removeCallbacks(searchRunnable)
        searchRequest(query)
    }

    fun showHistory() {
        handler.removeCallbacks(searchRunnable)
        val history = searchHistoryInteractor.getHistory()
        if (history.isNotEmpty()) {
            stateLiveData.value = SearchState.History(history)
        } else {
            stateLiveData.value = SearchState.Empty
        }
    }

    fun clearHistory() {
        searchHistoryInteractor.clear()
        stateLiveData.value = SearchState.Empty
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryInteractor.add(track)
        if (stateLiveData.value is SearchState.History) {
            showHistory()
        }
    }

    private fun searchRequest(query: String) {
        if (query.isBlank()) {
            return
        }
        stateLiveData.postValue(SearchState.Loading)
        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?) {
                val state = when {
                    foundTracks == null -> SearchState.ConnectionError
                    foundTracks.isEmpty() -> SearchState.NothingFound
                    else -> SearchState.Content(foundTracks)
                }
                stateLiveData.postValue(state)
            }
        })
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private var isClickAllowed = true

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(searchRunnable)
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}

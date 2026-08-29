package com.example.playlistmaker.ui.search.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TracksInteractor
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.search.SearchState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData<SearchState>(SearchState.Empty)
    private var searchJob: Job? = null
    private var isClickAllowed = true

    fun observeState(): LiveData<SearchState> = stateLiveData

    fun searchDebounce(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            searchRequest(query)
        }
    }

    fun searchImmediately(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { searchRequest(query) }
    }

    fun showHistory() {
        searchJob?.cancel()
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

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewModelScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    private suspend fun searchRequest(query: String) {
        if (query.isBlank()) return
        stateLiveData.value = SearchState.Loading
        tracksInteractor.searchTracks(query).collect { foundTracks ->
            stateLiveData.value = when {
                foundTracks == null -> SearchState.ConnectionError
                foundTracks.isEmpty() -> SearchState.NothingFound
                else -> SearchState.Content(foundTracks)
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}

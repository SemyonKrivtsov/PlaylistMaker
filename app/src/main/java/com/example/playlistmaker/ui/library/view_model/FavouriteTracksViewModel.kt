package com.example.playlistmaker.ui.library.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.library.FavouriteTracksInteractor
import com.example.playlistmaker.ui.library.FavouriteTracksState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FavouriteTracksViewModel(
    private val favouriteTracksInteractor: FavouriteTracksInteractor
) : ViewModel() {

    private var isClickAllowed = true
    private val stateLiveData =
        MutableLiveData<FavouriteTracksState>()

    fun observeState(): LiveData<FavouriteTracksState> = stateLiveData

    init {
        viewModelScope.launch {
            favouriteTracksInteractor.getFavouriteTracks().collect { tracks ->
                stateLiveData.value = if (tracks.isEmpty()) {
                    FavouriteTracksState.Empty
                } else {
                    FavouriteTracksState.Content(tracks)
                }
            }
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

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
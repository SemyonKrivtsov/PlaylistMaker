package com.example.playlistmaker.ui.player.view_model

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.library.FavouriteTracksInteractor
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.player.PlayerState
import com.example.playlistmaker.utils.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val track: Track,
    private val mediaPlayer: MediaPlayer,
    private val favouriteTracksInteractor: FavouriteTracksInteractor
) : ViewModel() {
    private val playerStateLiveData = MutableLiveData<PlayerState>(PlayerState.Default)
    private val favouriteLiveData = MutableLiveData(track.isFavourite)
    private var timerJob: Job? = null
    private var favouriteJob: Job? = null

    init {
        preparePlayer()
        favouriteJob = viewModelScope.launch {
            favouriteLiveData.value = favouriteTracksInteractor.isFavourite(track.trackId)
        }
    }

    fun observePlayerState(): LiveData<PlayerState> = playerStateLiveData
    fun observeFavourite(): LiveData<Boolean> = favouriteLiveData

    fun onPlayButtonClicked() {
        when (playerStateLiveData.value) {
            is PlayerState.Playing -> pausePlayer()
            is PlayerState.Paused, PlayerState.Prepared -> startPlayer()
            else -> Unit
        }
    }

    fun onFavouriteClicked() {
        favouriteJob?.cancel()
        val isFavourite = favouriteLiveData.value ?: false
        favouriteLiveData.value = !isFavourite
        viewModelScope.launch {
            if (isFavourite) {
                favouriteTracksInteractor.removeFromFavourites(track)
            } else {
                favouriteTracksInteractor.addToFavourites(track)
            }
        }
    }

    fun onPause() {
        if (playerStateLiveData.value is PlayerState.Playing) {
            pausePlayer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
    }

    private fun preparePlayer() {
        val url = track.previewUrl
        if (url.isNullOrBlank()) return
        mediaPlayer.setDataSource(url)
        mediaPlayer.setOnPreparedListener {
            playerStateLiveData.postValue(PlayerState.Prepared)
        }
        mediaPlayer.setOnCompletionListener {
            pauseTimer()
            playerStateLiveData.postValue(PlayerState.Prepared)
        }
        mediaPlayer.prepareAsync()
    }

    private fun startPlayer() {
        mediaPlayer.start()
        startTimerUpdate()
    }

    private fun pausePlayer() {
        pauseTimer()
        mediaPlayer.pause()
        playerStateLiveData.postValue(PlayerState.Paused(TimeFormatter.formatMillis(mediaPlayer.currentPosition.toLong())))
    }

    private fun startTimerUpdate() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (mediaPlayer.isPlaying) {
                playerStateLiveData.value =
                    PlayerState.Playing(TimeFormatter.formatMillis(mediaPlayer.currentPosition.toLong()))
                delay(TIMER_DELAY)
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
    }

    companion object {
        private const val TIMER_DELAY = 300L
    }
}

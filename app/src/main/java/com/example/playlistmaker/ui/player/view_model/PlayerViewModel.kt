package com.example.playlistmaker.ui.player.view_model

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.ui.player.PlayerState
import com.example.playlistmaker.utils.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val url: String,
    private val mediaPlayer: MediaPlayer
) : ViewModel() {
    private val playerStateLiveData = MutableLiveData<PlayerState>(PlayerState.Default)
    private var timerJob: Job? = null

    init {
        preparePlayer()
    }

    fun observePlayerState(): LiveData<PlayerState> = playerStateLiveData

    fun onPlayButtonClicked() {
        when (playerStateLiveData.value) {
            is PlayerState.Playing -> pausePlayer()
            is PlayerState.Paused, PlayerState.Prepared -> startPlayer()
            else -> Unit
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
        if (url.isBlank()) return
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

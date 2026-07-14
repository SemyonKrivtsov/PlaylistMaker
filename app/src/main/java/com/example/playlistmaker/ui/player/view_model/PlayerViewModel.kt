package com.example.playlistmaker.ui.player.view_model

import android.media.MediaPlayer
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.ui.player.PlayerState
import com.example.playlistmaker.utils.TimeFormatter

class PlayerViewModel(
    private val url: String,
    private val mediaPlayer: MediaPlayer,
    private val handler: Handler
) : ViewModel() {
    private val playerStateLiveData = MutableLiveData<PlayerState>(PlayerState.Default)
    fun observePlayerState(): LiveData<PlayerState> = playerStateLiveData

    private val timerRunnable = Runnable {
        if (playerStateLiveData.value is PlayerState.Playing) {
            startTimerUpdate()
        }
    }

    init {
        preparePlayer()
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
        pauseTimer()
    }

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

    private fun preparePlayer() {
        if (url.isBlank()) return
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerStateLiveData.postValue(PlayerState.Prepared)
        }
        mediaPlayer.setOnCompletionListener {
            playerStateLiveData.postValue(PlayerState.Prepared)
            pauseTimer()
        }
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
        playerStateLiveData.postValue(PlayerState.Playing(TimeFormatter.formatMillis(mediaPlayer.currentPosition.toLong())))
        handler.postDelayed(timerRunnable, TIMER_DELAY)
    }

    private fun pauseTimer() {
        handler.removeCallbacks(timerRunnable)
    }

    companion object {
        private const val TIMER_DELAY = 200L
    }
}

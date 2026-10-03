package com.example.playlistmaker.ui.player.view_model

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.library.FavouriteTracksInteractor
import com.example.playlistmaker.domain.library.PlaylistInteractor
import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.player.AddTrackResult
import com.example.playlistmaker.ui.player.PlayerState
import com.example.playlistmaker.utils.SingleLiveEvent
import com.example.playlistmaker.utils.TimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val track: Track,
    private val mediaPlayer: MediaPlayer,
    private val favouriteTracksInteractor: FavouriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {
    private val playerStateLiveData = MutableLiveData<PlayerState>(PlayerState.Default)
    private val favouriteLiveData = MutableLiveData(false)
    private var timerJob: Job? = null
    private var favouriteJob: Job? = null
    private var playlistsJob: Job? = null
    private var isPrepared = false

    private val playlistsLiveData = MutableLiveData<List<Playlist>>(emptyList())
    private val addTrackResultLiveData = SingleLiveEvent<AddTrackResult>()

    fun observePlayerState(): LiveData<PlayerState> = playerStateLiveData
    fun observeFavourite(): LiveData<Boolean> = favouriteLiveData
    fun observePlaylists(): LiveData<List<Playlist>> = playlistsLiveData
    fun observeAddTrackResult(): LiveData<AddTrackResult> = addTrackResultLiveData

    fun onViewCreated() {
        if (!isPrepared) {
            isPrepared = true
            preparePlayer()
        }
        checkFavourite()
        observePlaylistsFlow()
    }

    fun onPlaylistClicked(playlist: Playlist) {
        if (playlist.trackIds.contains(track.trackId)) {
            addTrackResultLiveData.setEvent(AddTrackResult.AlreadyAdded(playlist.title))
            return
        }
        viewModelScope.launch {
            playlistInteractor.addTrackToPlaylist(track, playlist)
            addTrackResultLiveData.setEvent(AddTrackResult.Added(playlist.title))
        }
    }

    private fun observePlaylistsFlow() {
        if (playlistsJob != null) return
        playlistsJob = viewModelScope.launch {
            playlistInteractor.getPlaylists().collect { playlists ->
                playlistsLiveData.value = playlists
            }
        }
    }

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

    private fun checkFavourite() {
        favouriteJob?.cancel()
        favouriteJob = viewModelScope.launch {
            favouriteLiveData.value = favouriteTracksInteractor.isFavourite(track.trackId)
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

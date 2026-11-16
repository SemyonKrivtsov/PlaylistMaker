package com.example.playlistmaker.ui.player.view_model

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
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
import com.example.playlistmaker.ui.player.service.MusicService
import com.example.playlistmaker.ui.player.service.PlayerControl
import com.example.playlistmaker.utils.SingleLiveEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val track: Track,
    private val favouriteTracksInteractor: FavouriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {
    private val playerStateLiveData = MutableLiveData<PlayerState>(PlayerState.Default)
    private val favouriteLiveData = MutableLiveData(false)
    private var favouriteJob: Job? = null
    private var playlistsJob: Job? = null

    private var playerControl: PlayerControl? = null
    private var playerStateJob: Job? = null
    private var isUiInBackground = false

    var isServiceBound = false
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicService.MusicServiceBinder ?: return
            setPlayerControl(binder.getService())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            removePlayerControl()
        }
    }

    private val playlistsLiveData = MutableLiveData<List<Playlist>>(emptyList())
    private val addTrackResultLiveData = SingleLiveEvent<AddTrackResult>()

    fun observePlayerState(): LiveData<PlayerState> = playerStateLiveData
    fun observeFavourite(): LiveData<Boolean> = favouriteLiveData
    fun observePlaylists(): LiveData<List<Playlist>> = playlistsLiveData
    fun observeAddTrackResult(): LiveData<AddTrackResult> = addTrackResultLiveData

    fun onViewCreated() {
        checkFavourite()
        observePlaylistsFlow()
    }

    private fun setPlayerControl(control: PlayerControl) {
        playerControl = control
        playerStateJob?.cancel()
        playerStateJob = viewModelScope.launch {
            control.getPlayerState().collect { state ->
                playerStateLiveData.value = state
                if (isUiInBackground && state !is PlayerState.Playing) {
                    control.hideNotification()
                }
            }
        }
    }

    private fun removePlayerControl() {
        playerStateJob?.cancel()
        playerStateJob = null
        playerControl = null
        playerStateLiveData.value = PlayerState.Default
    }

    fun onUiStarted() {
        isUiInBackground = false
        playerControl?.hideNotification()
    }

    fun onUiStopped() {
        isUiInBackground = true
        if (playerStateLiveData.value is PlayerState.Playing) {
            playerControl?.showNotification()
        }
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
            is PlayerState.Playing -> playerControl?.pausePlayer()
            is PlayerState.Paused, PlayerState.Prepared -> playerControl?.startPlayer()
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

    override fun onCleared() {
        super.onCleared()
        playerStateJob?.cancel()
        playerControl = null
    }
}

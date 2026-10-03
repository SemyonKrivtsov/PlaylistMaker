package com.example.playlistmaker.ui.playlist.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.library.PlaylistInteractor
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.domain.sharing.SharingInteractor
import com.example.playlistmaker.ui.playlist.PlaylistScreenState
import com.example.playlistmaker.ui.playlist.PlaylistShareTextFormatter
import com.example.playlistmaker.utils.SingleLiveEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PlaylistViewModel(
    private val playlistId: Long,
    private val playlistInteractor: PlaylistInteractor,
    private val sharingInteractor: SharingInteractor,
    private val shareTextFormatter: PlaylistShareTextFormatter
) : ViewModel() {

    private var isClickAllowed = true
    private var isDeleting = false

    private val stateLiveData = MutableLiveData<PlaylistScreenState>()
    private val nothingToShareEvent = SingleLiveEvent<Unit>()
    private val playlistDeletedEvent = SingleLiveEvent<Unit>()

    fun observeState(): LiveData<PlaylistScreenState> = stateLiveData
    fun observeNothingToShare(): LiveData<Unit> = nothingToShareEvent
    fun observePlaylistDeleted(): LiveData<Unit> = playlistDeletedEvent

    init {
        viewModelScope.launch {
            combine(
                playlistInteractor.getPlaylistById(playlistId).filterNotNull(),
                playlistInteractor.getPlaylistTracks(playlistId)
            ) { playlist, tracks ->
                PlaylistScreenState(
                    playlist = playlist,
                    tracks = tracks,
                    durationMinutes = TimeUnit.MILLISECONDS
                        .toMinutes(tracks.sumOf { it.trackTimeMillis })
                        .toInt()
                )
            }.collect { stateLiveData.value = it }
        }
    }

    fun onShareClicked() {
        val state = stateLiveData.value ?: return
        if (state.tracks.isEmpty()) {
            nothingToShareEvent.setEvent(Unit)
        } else {
            sharingInteractor.shareText(shareTextFormatter.format(state.playlist, state.tracks))
        }
    }

    fun deleteTrack(track: Track) {
        viewModelScope.launch {
            playlistInteractor.deleteTrackFromPlaylist(playlistId, track.trackId)
        }
    }

    fun deletePlaylist() {
        if (isDeleting) return
        isDeleting = true
        viewModelScope.launch {
            playlistInteractor.deletePlaylist(playlistId)
            playlistDeletedEvent.setEvent(Unit)
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

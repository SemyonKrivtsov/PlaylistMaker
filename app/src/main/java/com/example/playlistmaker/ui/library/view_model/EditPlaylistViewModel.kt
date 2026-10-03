package com.example.playlistmaker.ui.library.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.library.PlaylistInteractor
import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.utils.SingleLiveEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    playlistId: Long,
    playlistInteractor: PlaylistInteractor
) : NewPlaylistViewModel(playlistInteractor) {

    private var editedPlaylist: Playlist? = null
    private val playlistLoadedEvent = SingleLiveEvent<Playlist>()

    fun observePlaylistLoaded(): LiveData<Playlist> = playlistLoadedEvent

    init {
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId).first() ?: return@launch
            editedPlaylist = playlist
            coverUriLiveData.value = playlist.coverPath
            playlistLoadedEvent.setEvent(playlist)
        }
    }

    override fun createPlaylist() {
        val playlist = editedPlaylist ?: return
        val name = title.trim()
        if (name.isEmpty() || isCreating) return
        isCreating = true

        viewModelScope.launch {
            try {
                playlistInteractor.updatePlaylist(
                    playlist.copy(
                        title = name,
                        description = description.trim().ifEmpty { null },
                        coverPath = coverUriLiveData.value
                    )
                )
                playlistCreatedLiveData.setEvent(name)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                createErrorLiveData.setEvent(Unit)
            } finally {
                isCreating = false
            }
        }
    }
}

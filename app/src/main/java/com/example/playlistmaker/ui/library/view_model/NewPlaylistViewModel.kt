package com.example.playlistmaker.ui.library.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.library.PlaylistInteractor
import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.utils.SingleLiveEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private var title: String = ""
    private var description: String = ""
    private var isCreating = false

    private val coverUriLiveData = MutableLiveData<String?>(null)
    private val isCreateAvailableLiveData = MutableLiveData(false)
    private val playlistCreatedLiveData = SingleLiveEvent<String>()
    private val createErrorLiveData = SingleLiveEvent<Unit>()

    fun observeCoverUri(): LiveData<String?> = coverUriLiveData
    fun observeCreateAvailable(): LiveData<Boolean> = isCreateAvailableLiveData
    fun observePlaylistCreated(): LiveData<String> = playlistCreatedLiveData
    fun observeCreateError(): LiveData<Unit> = createErrorLiveData

    fun onTitleChanged(value: String) {
        title = value
        isCreateAvailableLiveData.value = value.isNotBlank()
    }

    fun onDescriptionChanged(value: String) {
        description = value
    }

    fun onCoverSelected(uri: String) {
        coverUriLiveData.value = uri
    }

    fun hasUnsavedData(): Boolean =
        title.isNotBlank() || description.isNotBlank() || coverUriLiveData.value != null

    fun createPlaylist() {
        val name = title.trim()
        if (name.isEmpty() || isCreating) return
        isCreating = true

        viewModelScope.launch {
            try {
                playlistInteractor.createPlaylist(
                    Playlist(
                        title = name,
                        description = description.trim().ifEmpty { null },
                        coverPath = coverUriLiveData.value,
                        trackIds = emptyList()
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

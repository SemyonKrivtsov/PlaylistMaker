package com.example.playlistmaker.ui.library

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.library.view_model.EditPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditPlaylistFragment : NewPlaylistFragment() {

    private val playlistId: Long by lazy { requireArguments().getLong(ARG_PLAYLIST_ID) }

    override val viewModel: EditPlaylistViewModel by viewModel { parametersOf(playlistId) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setTitle(R.string.edit)
        binding.createButton.setText(R.string.save)

        viewModel.observePlaylistLoaded().observe(viewLifecycleOwner) { playlist ->
            binding.playlistName.setText(playlist.title)
            binding.playlistDescription.setText(playlist.description.orEmpty())
        }
    }

    override fun onPlaylistSaved(name: String) {
        findNavController().navigateUp()
    }

    override fun onBackAction() {
        findNavController().navigateUp()
    }

    companion object {
        const val ARG_PLAYLIST_ID = "playlistId"
    }
}

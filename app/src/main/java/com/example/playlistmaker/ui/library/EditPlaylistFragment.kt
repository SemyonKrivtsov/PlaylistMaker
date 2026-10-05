package com.example.playlistmaker.ui.library

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.library.view_model.EditPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditPlaylistFragment : NewPlaylistFragment() {

    private val args: EditPlaylistFragmentArgs by navArgs()

    override val viewModel: EditPlaylistViewModel by viewModel { parametersOf(args.playlistId) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setTitle(R.string.edit)
        binding.createButton.setText(R.string.save)

        viewModel.observePlaylistLoaded().observe(viewLifecycleOwner) { playlist ->
            binding.playlistName.setText(playlist.title)
            binding.playlistDescription.setText(playlist.description.orEmpty())
        }
        viewModel.loadPlaylist()
    }

    override fun onPlaylistSaved(name: String) {
        findNavController().navigateUp()
    }

    override fun onBackAction() {
        findNavController().navigateUp()
    }
}
